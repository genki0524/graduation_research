#include "M5CoreS3.h"
#include "WiFi.h"
#include "WiFiUdp.h"
#include "string.h"
#include <WebSocketsClient.h>
#include "stb_image_resize.h"
int disp_w; // 画面幅格納用
int disp_h; // 画面高さ格納用
int cur_value = 1;
int last_value = 1;
bool send_data =false;
// const char ssid[] = "aterm-313d8b-g";
// const char passward[] = "22c0393355c14";
const char ssid[] = "CPSLAB_WLX";
const char pass[] = "6bepa8ideapbu";
WebSocketsClient webSocket;

void setClock() {
    configTime(0, 0, "pool.ntp.org", "time.nist.gov");

    Serial.print(F("Waiting for NTP time sync: "));
    time_t nowSecs = time(nullptr);
    while(nowSecs < 8 * 3600 * 2) {
        delay(500);
        Serial.print(F("."));
        yield();
        nowSecs = time(nullptr);
    }

    Serial.println();
    struct tm timeinfo;
    gmtime_r(&nowSecs, &timeinfo);
    Serial.print(F("Current time: "));
    Serial.print(asctime(&timeinfo));
}

void connectWiFi(){
  Serial.begin(115200);
  WiFi.begin(ssid,pass);
  while(WiFi.status() != WL_CONNECTED){
    delay(500);
    CoreS3.Display.print(".");
  }
  CoreS3.Display.println("WiFi connected");
  CoreS3.Display.print("IP address = ");
  CoreS3.Display.println(WiFi.localIP());
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
    switch(type) {
        case WStype_DISCONNECTED:
            Serial.printf("[WSc] Disconnected!\n");
            break;
        case WStype_CONNECTED:
            Serial.printf("[WSc] Connected to url: %s\n", payload); 
            break;
        case WStype_TEXT:
            Serial.printf("[WSc] get text: %s\n", payload);
            break;
        case WStype_ERROR:
        case WStype_FRAGMENT_TEXT_START:
        case WStype_FRAGMENT_BIN_START:
        case WStype_FRAGMENT:
        case WStype_FRAGMENT_FIN:
            break;
    }
}

// 初期設定 ------------------------------------------------------------------
void setup() {
  auto cfg = M5.config(); // 本体初期化
  CoreS3.begin(cfg);
  pinMode(8,INPUT);
  for(uint8_t t = 4; t > 0; t--) {
		Serial.printf("[SETUP] BOOT WAIT %d...\n", t);
		Serial.flush();
		delay(1000);
	}
  Serial.println("connected");

  connectWiFi();

  setClock();

  webSocket.begin("172.16.1.12", 1880,"/ws/m5CoreS3");

  webSocket.onEvent(webSocketEvent);

  webSocket.setReconnectInterval(5000);
  
  disp_w = CoreS3.Display.width();  // 画面幅取得
  disp_h = CoreS3.Display.height(); // 画面高さ取得

  CoreS3.Camera.begin(); // カメラ初期化
}

// メイン --------------------------------------------------------------------
void loop() {
  cur_value = digitalRead(8);
  Serial.print("cur_value:");
  Serial.println(cur_value);
  if(cur_value==0 && last_value==1){
    Serial.println("button is pressed");
    send_data = !send_data;
  }
  uint8_t* out_jpg;
  size_t out_len;
  // カメラからフレームを取得して表示
  if (CoreS3.Camera.get()) {
    if(send_data){
      frame2jpg(CoreS3.Camera.fb,40,&out_jpg,&out_len);
      webSocket.sendBIN(out_jpg,out_len);
      free(out_jpg);
    }
    CoreS3.Display.pushImage(0, 0, disp_w, disp_h,(uint16_t*)CoreS3.Camera.fb->buf); // QVGA表示 (x, y, w, h, *data)
    CoreS3.Camera.free(); // 取得したフレームを解放
  }
  webSocket.loop();
  last_value = cur_value;
  delay(33);
}