package com.coze.openapi.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.coze.openapi.client.auth.*;
import com.coze.openapi.service.utils.Utils;

import lombok.Getter;

public class PKCEOAuthClient extends OAuthClient {

  @Getter
  public enum CodeChallengeMethod {
    Plain("plain"),
    S256("S256");

    private final String value;

    CodeChallengeMethod(String value) {
      this.value = value;
    }
  }

  private static final int codeVerifierLen = 16;

  protected PKCEOAuthClient(PKCEOAuthBuilder builder) {
    super(builder);
  }

  public static PKCEOAuthClient loadFromConfig(LoadAuthConfig loadConfig) {
    OAuthConfig config = OAuthConfig.load(loadConfig);
    return new PKCEOAuthClient.PKCEOAuthBuilder()
        .clientID(config.getClientId())
        .baseURL(config.getCozeApiBase())
        .build();
  }

  public GetPKCEAuthURLResp genOAuthURL(@NotNull String redirectURI, String state) {
    return genOAuthURL(redirectURI, state, CodeChallengeMethod.Plain);
  }

  public GetPKCEAuthURLResp genOAuthURL(
      @NotNull String redirectURI, String state, @NotNull String workspaceID) {
    return genOAuthURL(redirectURI, state, CodeChallengeMethod.Plain, workspaceID);
  }

  public GetPKCEAuthURLResp genOAuthURL(
      @NotNull String redirectURI, String state, @NotNull CodeChallengeMethod codeChallengeMethod) {
    String codeVerifier = Utils.genRandomSign(codeVerifierLen);
    String url =
        super.getOAuthURL(
            redirectURI,
            state,
            getCode(codeVerifier, codeChallengeMethod),
            codeChallengeMethod.getValue());
    return new GetPKCEAuthURLResp(codeVerifier, url);
  }

  public GetPKCEAuthURLResp genOAuthURL(
      @NotNull String redirectURI,
      String state,
      @NotNull CodeChallengeMethod codeChallengeMethod,
      @NotNull String workspaceID) {
    String codeVerifier = Utils.genRandomSign(codeVerifierLen);
    String url =
        super.getOAuthURL(
            redirectURI,
            state,
            getCode(codeVerifier, codeChallengeMethod),
            codeChallengeMethod.getValue(),
            workspaceID);
    return new GetPKCEAuthURLResp(codeVerifier, url);
  }

  private String getCode(
      @NotNull String codeVerifier, @NotNull CodeChallengeMethod codeChallengeMethod) {
    String code = "";
    try {
      code =
          "plain".equals(codeChallengeMethod.getValue())
              ? codeVerifier
              : genS256CodeChallenge(codeVerifier);
    } catch (NoSuchAlgorithmException e) {
      code = codeVerifier;
    }
    return code;
  }

  public OAuthToken getAccessToken(
      @NotNull String code, @NotNull String redirectURI, @Nullable String codeVerifier) {
    GetAccessTokenReq req =
        GetAccessTokenReq.builder()
            .clientID(this.clientID)
            .grantType(GrantType.AUTHORIZATION_CODE.getValue())
            .code(code)
            .redirectUri(redirectURI)
            .codeVerifier(codeVerifier)
            .build();
    return super.getAccessToken(null, req);
  }

  @Override
  public OAuthToken refreshToken(String refreshToken) {
    return super.refreshAccessToken(refreshToken);
  }

  public static String genS256CodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] sha256Hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
    String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256Hash);
    codeChallenge = codeChallenge.replace("=", "");
    return codeChallenge;
  }

  public static class PKCEOAuthBuilder extends OAuthBuilder<PKCEOAuthBuilder> {
    @Override
    protected PKCEOAuthBuilder self() {
      return this;
    }

    @Override
    public PKCEOAuthClient build() {
      return new PKCEOAuthClient(this);
    }
  }
}
