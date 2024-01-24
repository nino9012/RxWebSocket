package com.dhh.rxwebsocket;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dhh.rxlifecycle.RxLifecycle;
import com.dhh.websocket.Config;
import com.dhh.websocket.RxWebSocket;
import com.dhh.websocket.WebSocketInfo;
import com.dhh.websocket.WebSocketSubscriber;
import com.dhh.websocket.WebSocketSubscriber2;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.ByteString;
import rx.Subscription;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private WebSocket mWebSocket;
    private EditText editText;
    private Button send;
    private Button centect;
    private Subscription mSubscription;
    private TextView textview;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        //init config
        Config config = new Config.Builder()
                .setShowLog(true)           //show  log
//                .setClient(yourClient)   //if you want to set your okhttpClient
//                .setShowLog(true, "your logTag")
//                .setReconnectInterval(2, TimeUnit.SECONDS)  //set reconnect interval
//                .setSSLSocketFactory(yourSSlSocketFactory, yourX509TrustManager) // wss support
                .build();

        RxWebSocket.setConfig(config);
        // please use WebSocketSubscriber
        RxWebSocket.get("wss://eoaptest.cebbank.com/uiap/wss")
                //RxLifecycle : https://github.com/dhhAndroid/RxLifecycle
                .compose(RxLifecycle.with(this).<WebSocketInfo>bindOnDestroy())
                .subscribe(new WebSocketSubscriber() {
                    @Override
                    public void onOpen(@NonNull WebSocket webSocket) {
                        Log.d("RXXXXX", "onOpen1:");
                        startHeart();
                        sendLogin();
                    }

                    @Override
                    public void onMessage(@NonNull String text) {
                        Log.d("RXXXXX", "返回数据:" + text);
                    }

                    @Override
                    public void onMessage(@NonNull ByteString byteString) {
                        Log.d("RXXXXX", "返回数据:" + byteString);

                    }

                    @Override
                    protected void onReconnect() {
                        Log.d("RXXXXX", "重连:");
                    }

                    @Override
                    protected void onClose() {
                        Log.d("RXXXXX", "onClose:");
                        super.onClose();
                    }
                });
        /**
         *
         *如果你想将String类型的text解析成具体的实体类，比如{@link List<String>},
         * 请使用 {@link  WebSocketSubscriber2}，仅需要将泛型传入即可
         */
//        RxWebSocket.get("your url")
//                .compose(RxLifecycle.with(this).<WebSocketInfo>bindToLifecycle())
//                .subscribe(new WebSocketSubscriber2<List<String>>() {
//                    @Override
//                    protected void onMessage(List<String> strings) {
//
//                    }
//                });
//
//        mSubscription = RxWebSocket.get("ws://sdfsd")
//                .subscribe(new WebSocketSubscriber() {
//                    @Override
//                    protected void onClose() {
//                        Log.d("MainActivity", "直接关闭");
//                    }
//                });

        initDemo();
    }

    Handler mHandler;
    Runnable mRunnable;

    Handler offLineHandler;
    Runnable offLineRunnable;

    private void startHeart() {
        if (mHandler != null) return;
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {

                mHandler.postDelayed(this, 10000);
                sendHeart();
            }

        };
        mHandler.post(mRunnable);
//        if (offLineHandler == null) {
//            offLineHandler = new Handler();
//            offLineRunnable = new Runnable() {
//                @Override
//                public void run() {
////                    setOnlineStatus(false);
//                }
//            };
//            offLineHandler.postDelayed(offLineRunnable, 15000);
//        }

    }

    private void sendHeart() {
        Map map = new HashMap();
        map.put("signal", "PING");
        map.put("subSignal", "NONE");
        map.put("messageId", 1);// 消息ID
        map.put("from", "0");// 登录人员类型：0 患者端，1医生端
        map.put("content", "heartCheck");
        Gson gson = new Gson();
        send(gson.toJson(map));


    }

    private void sendLogin() {
        Map map = new HashMap();
        map.put("signal", "CONNECT_ACK");
        map.put("subSignal", "NONE");
        map.put("messageId", 1);// 消息ID
        map.put("from", "0");// 登录人员类型：0 患者端，1医生端
        Map content = new HashMap();
        content.put("userToken", "E76D7A1CE5130EC0E05316D8C80AB0E7");// 登录用户标识
        content.put("terminal", "ws");
        content.put("platform", "web");
        map.put("content", content);

        Gson gson = new Gson();
        send(gson.toJson(map));
    }

    public void send(String jsonStr) {
        Gson gson = new Gson();
        Log.i("RXXXXX", "send: " + jsonStr);
        RxWebSocket.asyncSend("wss://eoaptest.cebbank.com/uiap/wss", jsonStr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        RxWebSocket.get("url")
//                .subscribe(new WebSocketSubscriber() {
//                    @Override
//                    protected void onMessage(@NonNull String text) {
//
//                    }
//                });
//
//        RxWebSocket.get("your url")
//                //RxLifecycle : https://github.com/dhhAndroid/RxLifecycle
//                .compose(RxLifecycle.with(this).<WebSocketInfo>bindToLifecycle())
//                .subscribe(new WebSocketSubscriber() {
//                    @Override
//                    public void onOpen(@NonNull WebSocket webSocket) {
//                        Log.d("RXXXXX", "onOpen1:");
//                    }
//
//                    @Override
//                    public void onMessage(@NonNull String text) {
//                        Log.d("RXXXXX", "返回数据:" + text);
//                    }
//
//                    @Override
//                    public void onMessage(@NonNull ByteString byteString) {
//
//                    }
//
//                    @Override
//                    protected void onReconnect() {
//                        Log.d("RXWebSocket", "重连:");
//                    }
//
//                    @Override
//                    protected void onClose() {
//                        Log.d("RXWebSocket", "onClose:");
//                    }
//                });
    }

    private void initDemo() {
        Schedulers.io().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                initServerWebsocket();
            }
        });
        setListener();

        // unsubscribe
        Subscription subscription = RxWebSocket.get("wss://rpc.heyunnetwork.com/diagnosis/socket").subscribe();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void initServerWebsocket() {
        final MockWebServer mockWebServer = new MockWebServer();
        url = "ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort() + "/";
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send("hello, I am  dhhAndroid !");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("MainActivity", "收到客户端消息:" + text);
                webSocket.send("Server response:" + text);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            }
        }));
    }


    private void setListener() {
        //send message
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                if (mWebSocket != null) {
                    mWebSocket.send(msg);
                } else {
                    send();
                }
            }
        });
        centect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注意取消订阅,有多种方式,比如 rxlifecycle
                connect();

            }
        });

    }

    private void connect() {
        RxWebSocket.get(url)
                //RxLifecycle : https://github.com/dhhAndroid/RxLifecycle
                .compose(RxLifecycle.with(this).<WebSocketInfo>bindOnDestroy())
                .subscribe(new WebSocketSubscriber() {
                    @Override
                    protected void onOpen(@NonNull WebSocket webSocket) {
                        Log.d("MainActivity", " on WebSocket open");
                    }

                    @Override
                    protected void onMessage(@NonNull String text) {
                        Log.d("MainActivity", text);
                        textview.setText(Html.fromHtml(text));
                    }

                    @Override
                    protected void onMessage(@NonNull ByteString byteString) {
                        Log.d("MainActivity", byteString.toString());
                    }

                    @Override
                    protected void onReconnect() {
                        Log.d("MainActivity", "onReconnect");
                    }

                    @Override
                    protected void onClose() {
                        Log.d("MainActivity", "onClose");
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }

    public void send() {
        //url 对应的WebSocket 必须打开,否则报错
        RxWebSocket.send(url, "hello");
        RxWebSocket.send(url, ByteString.EMPTY);
        //异步发送,若WebSocket已经打开,直接发送,若没有打开,打开一个WebSocket发送完数据,直接关闭.
        RxWebSocket.asyncSend(url, "hello");
        RxWebSocket.asyncSend(url, ByteString.EMPTY);
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.editText);
        send = (Button) findViewById(R.id.send);
        centect = (Button) findViewById(R.id.centect);
        textview = (TextView) findViewById(R.id.textview);
    }

}
