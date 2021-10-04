package com.kiebot.paygate.web.rest;

import com.kiebot.paygate.domain.UserData;
import com.kiebot.paygate.repository.UserDataRepository;
import java.io.IOException;
import java.util.Objects;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * REST controller for managing {@link com.kiebot.paygate.domain.UserData}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class UserDataResource {

    private final Logger log = LoggerFactory.getLogger(UserDataResource.class);
    public final String clientID = "je9b53p5syz1qnav9cozg6u61jrfojs";
    public final String clientSecret = "afb8227f93aaf6a19c0785c709f0dfc130d172395e7fe3f228016dcbce6e0978";
    public final String host = "https://1010-103-147-209-102.ngrok.io";
    public final OkHttpClient client = new OkHttpClient();
    public final UserDataRepository userDataRepository;

    public UserDataResource(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    /**
     *
     * Bigcommerce will connect this end-point with a GET request when user grand the requested permissions.
     * @param code Temporary access code of bigcommerce API
     * @param scope List of OAuth scopes
     * @param context Store hash received in the GET request.
     *
     * @return HTML content which will be rendered in an iframe on Bigcommerce Admin portal
     *
     */
    @GetMapping("/auth")
    public ModelAndView init(
        @RequestParam("code") String code,
        @RequestParam(value = "scope") String scope,
        @RequestParam(value = "context") String context
    ) {
        System.out.println("redirect:" + host);
        context = context == null ? "" : context;
        scope = scope == null ? "" : scope;
        String storeHash = context.replace("stores/", "");
        Request tokenRequest = new Request.Builder()
            .url("https://login.bigcommerce.com/oauth2/token")
            .post(
                new FormBody.Builder()
                    .add("client_id", clientID)
                    .add("client_secret", clientSecret)
                    .add("code", code)
                    .add("scope", scope)
                    .add("grant_type", "authorization_code")
                    .add("redirect_uri", host + "/api/auth")
                    .add("context", context)
                    .build()
            )
            .build();
        try {
            Response response = client.newCall(tokenRequest).execute();
            JSONObject obj = new JSONObject(Objects.requireNonNull(response.body()).string());
            response.close();
            UserData data = new UserData();
            data.setToken(obj.getString("access_token"));
            userDataRepository.save(data);
            return new ModelAndView("redirect:" + host + "/update/" + data.getId());
        } catch (IOException e) {
            return new ModelAndView("redirect:" + host + "/404.html");
        }
    }
}
