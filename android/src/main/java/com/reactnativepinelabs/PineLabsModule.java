package com.reactnativepinelabs;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;


import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import static com.pine.plural_sdk.Constants.PluralPGConfig.createRequestForPinePG;
import static com.pine.plural_sdk.utli.Checksum.getXVerifyHeader;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.google.gson.Gson;
import com.kotak.bnpl.pineLabsPaymentSDK.model.InquiryResponse;
import com.kotak.bnpl.pineLabsPaymentSDK.service.InquiryAPIService;
import com.pine.plural_sdk.ApiLayer.APIClient;
import com.pine.plural_sdk.ApiLayer.PinePGService;
import com.pine.plural_sdk.Callbacks.Interface.IPinePGResponseCallback;
import com.pine.plural_sdk.Callbacks.PinePGPaymentManager;
import com.pine.plural_sdk.Constants.Environment;
import com.pine.plural_sdk.modals.CreateOrderRequest;
import com.pine.plural_sdk.modals.CreateOrderResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@ReactModule(name = PineLabsModule.NAME)
public class PineLabsModule extends ReactContextBaseJavaModule {
    public static final String NAME = "PineLabs";

    PinePGPaymentManager objPinePGPaymentManager = null;
    String PLURAL_MERCHANT_SECURE_SECRET_KEY; 
    String PLURAL_MERCHANT_ID ;
    String PLURAL_MERCHANT_ACCESS_CODE; 

    static String XVerifyHeader;
    Promise _promise;


    public PineLabsModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }


    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    public void multiply(double a, double b, Promise promise) {
        promise.resolve(a * b);
    }
 



    private boolean skdInitated () { 
        return PLURAL_MERCHANT_SECURE_SECRET_KEY != null && PLURAL_MERCHANT_ID != null && PLURAL_MERCHANT_ACCESS_CODE != null  ; 
    }

    @ReactMethod
    public void initSDK(String merchantSecret, String merchantId, String merchantAccessCode, Promise promise) {
        WritableMap params = Arguments.createMap();
       
        if(merchantSecret == null || merchantId == null || merchantAccessCode == null ){ 
            params.putString("error","merchantSecret, merchantId,merchantAccessCode can not be null");
            promise.reject("error","merchantSecret, merchantId,merchantAccessCode can not be null");
        }
        PLURAL_MERCHANT_SECURE_SECRET_KEY = merchantSecret;
        PLURAL_MERCHANT_ID = merchantId;
        PLURAL_MERCHANT_ACCESS_CODE = merchantAccessCode;  
        
        params.putBoolean("success",true); 
        promise.resolve(params);

    }

    /**
     * @description 
     */
    @ReactMethod
    public void createOrderToken(String jsonRequest, Promise promise) {
        _promise = promise;
        objPinePGPaymentManager = new PinePGPaymentManager();
        Environment env = Environment.UAT;
        createRequestForPinePG(env);
        WritableMap params = Arguments.createMap();
        if(skdInitated()){
            
            byte[] encodeValue = Base64.encode(jsonRequest.getBytes(), Base64.DEFAULT);
            String jsonRequestBase64 = new String(encodeValue);
            jsonRequestBase64 = jsonRequestBase64.replace("\n", "");
    
            try {
                XVerifyHeader = generateXVerifyHeader(jsonRequest, jsonRequestBase64);
            } catch (JSONException e) {
                e.printStackTrace();
                
                   params.putString("failure", "Duplicate id passed");
                _promise.resolve(params);
            } 
    
            byte[] authorisationHeaderBytes = (PLURAL_MERCHANT_ID + ":" + PLURAL_MERCHANT_ACCESS_CODE).getBytes(StandardCharsets.UTF_8);
            String encodedAuthorisationHeader = Base64.encodeToString(authorisationHeaderBytes, Base64.NO_WRAP);
             generateOrderToken(encodedAuthorisationHeader, jsonRequestBase64);

        }else{
            _promise.reject("error","SDK intilisation failed") ;

        }

       
    }

    private String generateXVerifyHeader(String jsonRequest, String jsonRequestBase64) throws JSONException {
        return getXVerifyHeader(PLURAL_MERCHANT_SECURE_SECRET_KEY, jsonRequest, jsonRequestBase64);
    }

    private void generateOrderToken(String encodedAuthorisationHeader, String jsonRequestBase64) {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(jsonRequestBase64);
        PinePGService apiInterface = APIClient.getClient().create(PinePGService.class);
        Call<CreateOrderResponse> call = apiInterface.callCreateOrderApi("Basic " + encodedAuthorisationHeader, "application/json", XVerifyHeader, createOrderRequest);

        call.enqueue(new retrofit2.Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateOrderResponse> call, @NonNull retrofit2.Response<CreateOrderResponse> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    if (response.body() != null) {
                        initialisePineLabsSDK(response.body().token);
                    } else {
                        _promise.resolve(new Gson().toJson(response));
                    }
                } else {
                    _promise.resolve(new Gson().toJson(response));
                }
                Log.d("TAG_RESPONSE", "response of payment : " + response);
            }

            @Override
            public void onFailure(@NonNull Call<CreateOrderResponse> call, @NonNull Throwable t) {
                Log.d("TAG_RESPONSE", "response of payment : " + t);
                _promise.reject(buildErrorJson(t.getMessage(), 0));
            }
        });
    }

    private void initialisePineLabsSDK(String orderToken) {
        JSONObject options = getJsonOptions(orderToken);
        objPinePGPaymentManager.startPayment(getCurrentActivity(), options, new IPinePGResponseCallback() {
            @Override
            public void onErrorOccured(int code, String message) {
                _promise.reject(buildErrorJson(message, code));
                Log.d("TAG_RESPONSE", "onErrorOccured : " + message);
            }

            @Override
            public void onTransactionResponse(JSONObject jsonObject) {
                Log.d("TAG_RESPONSE", "onTransactionResponse : " + jsonObject);
                try {
                    //  _promise.resolve(jsonObject);
                    String paymentId = jsonObject.getString("payment_id");
                    String orderId = jsonObject.getString("plural_order_id");
                    initialiseInquiryAPI(orderId, paymentId);
                } catch (JSONException e) {
                    e.printStackTrace();
                    _promise.reject(buildErrorJson(e.getMessage(), 0));
                }
            }

            @Override
            public void onCancelTxn(int code, String message) {
                WritableMap params = Arguments.createMap();
               params.putString("cancel", message);
               _promise.resolve(params);
                Log.d("TAG_RESPONSE", "onCancelTxn : " + message);
                // Log.d(params);
            }
        });
        
    }

    private String buildErrorJson(String message, int code) {
        return  "{message:" + message + ", code:" + code + " }";
    }

    private JSONObject getJsonOptions(String orderToken) {
        JSONObject options = new JSONObject();
        try {
            options.put("emailId", "xyz@pinelabs.com");
            options.put("countryCode", "91");
            options.put("mobileNumber", "1234567890");
            options.put("showSavedCardsFeature", false);
            options.put("orderToken", orderToken);
            options.put("channelId", "APP");
            options.put("theme", "default");
            options.put("paymentMode", "ALL");
            return options;
        } catch (JSONException e) {
            // _promise.reject(buildErrorJson(e.getMessage(), 0));
            e.printStackTrace();
            return null;
        }
    }

    private void initialiseInquiryAPI(String orderId, String paymentId) {
         Log.d("TAG_RESPONSE", "InitializeResponse : " );
        InquiryAPIService apiInterface = APIClient.getClient().create(InquiryAPIService.class);
        Call<InquiryResponse> call = apiInterface.enquiryAPI(orderId, paymentId);
        call.enqueue(new Callback<InquiryResponse>() {
            @Override
            public void onResponse(@NonNull Call<InquiryResponse> call, @NonNull Response<InquiryResponse> response) {
                Log.d("TAG_RESPONSE", "InitializeResponseSuccess : " + new Gson().toJson(response.body()));
                // _promise.resolve(new Gson().toJson(response));
                 WritableMap params = Arguments.createMap();
                  params.putString("success", new Gson().toJson(response.body()));
                _promise.resolve(params);
                
            }

            @Override
            public void onFailure(@NonNull Call<InquiryResponse> call, @NonNull Throwable t) {
                // Log.d("TAG_RESPONSE", "response of payment : " + t);
                WritableMap params = Arguments.createMap();
                  params.putString("failure","Transaction Failed");
                 _promise.resolve(params);
            }
        });
    }





}
