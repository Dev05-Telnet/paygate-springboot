package com.kiebot.paygate.web.rest;

import com.kiebot.paygate.domain.Transaction;
import com.kiebot.paygate.domain.UserData;
import com.kiebot.paygate.repository.TransactionRepository;
import com.kiebot.paygate.repository.UserDataRepository;
import com.kiebot.paygate.utils.Utils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
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
    public final String host = "https://bigcommerce-paygate.herokuapp.com";
    public final OkHttpClient client = new OkHttpClient();
    public final UserDataRepository userDataRepository;
    public final TransactionRepository transactionRepository;

    @SuppressWarnings("FieldCanBeLocal")
    private final String paymentName = "Paygate";

    String return_url = host + "/api/completed";
    String locale = "en-za";
    String country = "ZAF";
    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

    public UserDataResource(UserDataRepository userDataRepository, TransactionRepository transactionRepository) {
        this.userDataRepository = userDataRepository;
        this.transactionRepository = transactionRepository;
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
            System.out.println(obj);
            response.close();
            UserData data = new UserData();
            context = context.replace("stores/", "");
            data.token(obj.getString("access_token")).store(context).userId(obj.getJSONObject("user").getInt("id"));
            userDataRepository.save(data);

            return new ModelAndView("redirect:" + host + "/update/" + data.getId());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ModelAndView("redirect:" + host + "/404.html");
        }
    }

    @GetMapping("/load")
    public ModelAndView load(@RequestParam("signed_payload") String payload) {
        String data = payload.split("\\.")[0];
        JSONObject userInfo = new JSONObject(new String(Base64.getDecoder().decode(data)));
        int userId = userInfo.getJSONObject("user").getInt("id");
        UserData user = userDataRepository.getDataByUserId(userId);
        return user != null
            ? new ModelAndView("redirect:" + host + "/update/" + user.getId() + "?script=false")
            : new ModelAndView("redirect:" + host + "/404.html");
    }

    @PostMapping("update/{id}")
    @CrossOrigin("*")
    public ResponseEntity<String> update(
        @PathVariable Long id,
        @RequestParam("paygateId") String payId,
        @RequestParam("paygateSecret") String paySecret
    ) {
        UserData data = userDataRepository.getOne(id);
        String storeHash = data.getStore();
        data.setPayGateID(payId);
        data.setPayGateSecret(paySecret);
        userDataRepository.save(data);
        try {
            if (data.getScriptId() != null && scriptExists(data)) return ResponseEntity.ok("ok");
            String key = data.getToken();
            String url = "https://api.bigcommerce.com/stores/" + storeHash + "/v3/content/scripts";
            String script = Utils.getInjectionScript(host, data.getId());
            JSONObject requestBody = new JSONObject();
            requestBody
                .put("name", "paygate-payment-gateway")
                .put("Discription", "Checkout script for integrating Paygate in bigcommerce")
                .put("html", script)
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
            Request createScript = new Request.Builder()
                .url(url)
                .addHeader("X-Auth-Token", key)
                .addHeader("Accept", " application/json")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            Response response = client.newCall(createScript).execute();
            JSONObject scriptData = new JSONObject(Objects.requireNonNull(response.body()).string());
            String uuid = scriptData.getJSONObject("data").getString("uuid");
            data.setScriptId(uuid);
            userDataRepository.save(data);
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
        @PathVariable("orderId") int orderId
    ) {
        try {
            UserData user = userDataRepository.getOne(id);
            JSONObject order = getOrder(orderId, user);
            if (
                !order.getString("payment_method").equals(paymentName) ||
                !order.getString("status").equals("Awaiting Payment")
            ) return ResponseEntity.ok(new HashMap<>());
            long orderAmount = ((Double) Double.parseDouble(order.getString("total_inc_tax"))).longValue() * 100;
            String currency = order.getString("currency_code");
            String mail = order.getJSONObject("billing_address").getString("email");
            String payGateId = user.getPayGateID();
            String payGateSecret = user.getPayGateSecret();
            String reference = user.getId().toString();
            String notifyUrl = host + "/api/paygate/update/" + user.getId() + "/" + orderId;
            String checksum = Utils.md5(
                payGateId +
                reference +
                orderAmount +
                currency +
                return_url +
                date +
                locale +
                country +
                mail +
                notifyUrl +
                payGateSecret
            );
            assert checksum != null;
            Request paygateInit = new Request.Builder()
                .url("https://secure.paygate.co.za/payweb3/initiate.trans")
                .post(
                    new FormBody.Builder()
                        .add("PAYGATE_ID", payGateId)
                        .add("REFERENCE", reference)
                        .add("AMOUNT", Long.toString(orderAmount))
                        .add("CURRENCY", currency)
                        .add("RETURN_URL", return_url)
                        .add("TRANSACTION_DATE", date)
                        .add("LOCALE", locale)
                        .add("COUNTRY", country)
                        .add("EMAIL", mail)
                        .add("CHECKSUM", checksum)
                        .add("NOTIFY_URL", notifyUrl)
                        .build()
                )
                .build();
            Response paygateInitResponse = client.newCall(paygateInit).execute();
            HashMap<String, String> map = Utils.formToMap(Objects.requireNonNull(paygateInitResponse.body()).string());
            HashMap<String, String> resMap = new HashMap<>();
            resMap.put("PAY_REQUEST_ID", map.get("PAY_REQUEST_ID"));
            resMap.put("CHECKSUM", map.get("CHECKSUM"));
            resMap.put("REFERENCE", map.get("REFERENCE"));
            Transaction transaction = new Transaction();
            transaction.setOrderId(orderId);
            transaction.setPayRequestId(map.get("PAY_REQUEST_ID"));
            transactionRepository.save(transaction);
            paygateInitResponse.close();
            return ResponseEntity.ok(resMap);
        } catch (IOException ignored) {
            return ResponseEntity.internalServerError().body(new HashMap<>());
        }
    }

    @PostMapping("/completed")
    public ModelAndView completed() {
        return new ModelAndView("redirect:" + host + "/content/complete.html");
    }

    @PostMapping("/status/{id}/{orderId}")
    public ResponseEntity<String> status(@PathVariable("id") Long id, @PathVariable("orderId") int orderId) {
        try {
            UserData user = userDataRepository.getOne(id);
            JSONObject order = getOrder(orderId, user);
            if (!order.getString("status").equals("Awaiting Payment")) return ResponseEntity.ok("ok");
            System.out.println("beenHere");
            Transaction transaction = transactionRepository.getTransactionByOrder(orderId);
            String payGateId = user.getPayGateID();
            String payGateSecret = user.getPayGateSecret();
            String payRequestId = transaction.getPayRequestId();
            String reference = user.getId().toString();
            String checksum = Utils.md5(payGateId + payRequestId + reference + payGateSecret);
            assert checksum != null;
            Request query = new Request.Builder()
                .url("https://secure.paygate.co.za/payweb3/query.trans")
                .post(
                    new FormBody.Builder()
                        .add("PAYGATE_ID", payGateId)
                        .add("PAY_REQUEST_ID", payRequestId)
                        .add("REFERENCE", orderId + "")
                        .add("CHECKSUM", checksum)
                        .build()
                )
                .build();
            Response response = client.newCall(query).execute();
            HashMap<String, String> map = Utils.formToMap(Objects.requireNonNull(response.body()).string());
            int status = getStatus(Integer.parseInt(map.get("TRANSACTION_STATUS")));
            updateOrderStatus(orderId, user, status);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/paygate/update/{id}/{orderId}")
    public ResponseEntity<String> update(
        @PathVariable("id") Long id,
        @PathVariable("orderId") int orderId,
        @org.springframework.web.bind.annotation.RequestBody String body
    ) {
        try {
            UserData user = userDataRepository.getOne(id);
            HashMap<String, String> map = Utils.formToMap(body);
            int status = getStatus(Integer.parseInt(map.get("TRANSACTION_STATUS")));
            updateOrderStatus(orderId, user, status);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("OK");
    }

    int getStatus(int transactionStatus) {
        int status = 1;
        switch (transactionStatus) {
            case 1:
            case 5:
                status = 11;
                break;
            default:
                status = 6;
                break;
        }
        return status;
    }

    boolean scriptExists(UserData user) throws IOException {
        Request request = new Request.Builder()
            .url("https://api.bigcommerce.com/stores/" + user.getStore() + "/v3/content/scripts/" + user.getScriptId())
            .get()
            .addHeader("accept", "application/json")
            .addHeader("content-type", "application/json")
            .addHeader("x-auth-token", user.getToken())
            .build();

        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    void updateOrderStatus(int orderId, UserData data, int status) throws IOException {
        Request request = new Request.Builder()
            .addHeader("X-Auth-Token", data.getToken())
            .addHeader("Accept", "application/json")
            .url("https://api.bigcommerce.com/stores/" + data.getStore() + "/v2/orders/" + orderId)
            .put(
                okhttp3.RequestBody.create(
                    new JSONObject().put("status_id", status).toString(),
                    MediaType.parse("application/json; charset=utf-8")
                )
            )
            .build();
        Response response = client.newCall(request).execute();
    }

    JSONObject getOrder(int orderId, UserData user) throws IOException {
        String key = user.getToken();
        String url = "https://api.bigcommerce.com/stores/" + user.getStore() + "/v2/orders/" + orderId;
        Request order = new Request.Builder()
            .url(url)
            .addHeader("X-Auth-Token", key)
            .addHeader("Accept", " application/json")
            .addHeader("Content-Type", "application/json")
            .get()
            .build();
        Response response = client.newCall(order).execute();
        return new JSONObject(Objects.requireNonNull(response.body()).string());
    }
}
