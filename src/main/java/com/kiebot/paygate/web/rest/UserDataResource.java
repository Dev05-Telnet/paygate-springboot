package com.kiebot.paygate.web.rest;

import com.kiebot.paygate.domain.UserData;
import com.kiebot.paygate.repository.UserDataRepository;
import com.kiebot.paygate.utils.Utils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import okhttp3.*;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
    public final String host = "https://dbcd-103-154-54-126.ngrok.io";
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
    public ModelAndView bigcommerceAuth(
        @RequestParam("code") String code,
        @RequestParam("scope") String scope,
        @RequestParam("context") String context
    ) {
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
            context = context.replace("stores/", "");
            data.payGateID(context);
            data.referId(obj.getJSONObject("user").getInt("id"));
            userDataRepository.save(data);

            return new ModelAndView("redirect:" + host + "/update/" + data.getId());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ModelAndView("redirect:" + host + "/404.html");
        }
    }

    @PostMapping("update/{id}")
    @CrossOrigin("*")
    public ResponseEntity<String> update(
        @PathVariable Long id,
        @RequestParam("paygateId") String payId,
        @RequestParam("paygateSecret") String paySecret
    ) {
        UserData data = userDataRepository.getDataById(id);
        String storeHash = data.getPayGateID();
        data.setPayGateID(payId);
        data.setPayGateSecret(paySecret);
        userDataRepository.save(data);

        String key = data.getToken();
        String url = "https://api.bigcommerce.com/stores/" + storeHash + "/v3/content/scripts";
        JSONObject requestBody = new JSONObject();
        requestBody
            .put("name", "paygate-checkout")
            .put("Discription", "Checkout script for integrating Paygate in bigcommerc")
            .put(
                "html",
                "<script>\n" +
                "    var orderId = {{checkout.order.id}};\n" +
                "    console.log(orderId);\n" +
                "</script>"
            )
            .put("auto_uninstall", true)
            .put("load_method", "default")
            .put("location", "footer")
            .put("visibility", "order_confirmation")
            .put("kind", "script_tag")
            .put("consent_category", "essential");
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json; charset=utf-8")
        );
        Request createScript = new Request.Builder().url(url).addHeader("X-Auth-Token", key).post(body).build();
        try {
            Response response = client.newCall(createScript).execute();
            return ResponseEntity
                .ok()
                .body(response.isSuccessful() ? "ok" : Objects.requireNonNull(response.body()).string());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return ResponseEntity.ok().body("Something went wrong!");
        }
    }

    @PostMapping("/process/{id}/{orderId}")
    public ResponseEntity<HashMap<String, String>> process(
        @PathVariable("id") Long id,
        @PathVariable("orderId") Long orderId
    ) {
        //        UserData data = userDataRepository.getDataById(id);
        //        String payGateId = data.getPayGateID();
        //        String payGateSecret = data.getPayGateSecret();
        //        String reference = data.getId().toString();
        //        String checksum = Utils.md5(
        //            payGateId +
        //                reference +
        //                amount +
        //                currency +
        //                return_url +
        //                date +
        //                locale +
        //                country +
        //                mail +
        //                notifyUrl +
        //                payGateSecret
        //        );
        //        Request paygateInit = new Request.Builder()
        //            .url("https://secure.paygate.co.za/payweb3/initiate.trans")
        //            .post(
        //                new FormBody.Builder()
        //                    .add("PAYGATE_ID", payGateId)
        //                    .add("REFERENCE", reference)
        //                    .add("AMOUNT", amount)
        //                    .add("CURRENCY", currency)
        //                    .add("RETURN_URL", return_url)
        //                    .add("TRANSACTION_DATE", date)
        //                    .add("LOCALE", locale)
        //                    .add("COUNTRY", country)
        //                    .add("EMAIL", mail)
        //                    .add("CHECKSUM", checksum)
        //                    .add("NOTIFY_URL", notifyUrl)
        //                    .build()
        //            )
        //            .build();
        //        try {
        //            Response paygateInitResponse = client.newCall(paygateInit).execute();
        //            HashMap<String, String> map = formToMap(Objects.requireNonNull(paygateInitResponse.body()).string());
        //            paygateInitResponse.close();
        //            Transaction transaction = new Transaction();
        //            transaction.setPayRequestID(map.get("PAY_REQUEST_ID"));
        //            transaction.setRefererID(reference);
        //            transaction.setUserID(appId.toString());
        //            transaction.setStatus("PENDING");
        //            map.put("reference", reference);
        //            map.remove("PAYGATE_ID  ");
        //            map.put("CHECKSUM", Utils.md5(payGateId + map.get("PAY_REQUEST_ID") + reference + payGateSecret));
        //            transactionRepository.save(transaction);
        //            return ResponseEntity.ok().body(map);
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //            return ResponseEntity.internalServerError().build();
        //        }
        return ResponseEntity.ok(new HashMap<>());
    }

    Long getOrderAmount(Long orderId, String key) {}
}
