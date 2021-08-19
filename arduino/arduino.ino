#include <DHT11.h>
#include<SPI.h>
#include <Wire.h>

int dust_pin = 8;
int gas_pin=A2;
int buzzer_pin=6;
int flame_pin=7;
int dht11_pin=5;
int blue_pin=4;
int red_pin=12;

unsigned long duration;
unsigned int gas;
unsigned int flame;
unsigned long starttime;
unsigned long pre_time;
unsigned long cur_time;
unsigned long sampletime_ms = 10000;//sampe 30s ;
unsigned long sampletime_flame=5000;
unsigned long lowpulseoccupancy = 0;
float ratio = 0;
float concentration = 0;
float ugm3 = 0;
float temp,humi;
DHT11 dht11(dht11_pin);
void setup() {
  Serial.begin(9600);
  pinMode(dust_pin,INPUT);
  pinMode(gas_pin,INPUT);
  pinMode(buzzer_pin,OUTPUT);
  pinMode(flame_pin,INPUT);

  starttime = millis();//get the current time;   
  pre_time=millis();
 
  pinMode(blue_pin,OUTPUT);
  pinMode(red_pin,OUTPUT);
  pinMode(green_pin,OUTPUT);
  digitalWrite(red_pin,LOW);
  digitalWrite(blue_pin,HIGH);
  digitalWrite(buzzer_pin,LOW); 
}
void loop() {
  int res;
  //불꽃 센서로 불꽃 인식했을 떄 2초동안 부저가 울리고 10초 주기마다 불꽃을 인식하여 반복(부저시간때문에 8초)
  //current_time=millis(); //화염
  duration = pulseIn(dust_pin, LOW);
  gas=analogRead(gas_pin);
  flame=digitalRead(flame_pin);
  res=dht11.read(humi,temp);
  lowpulseoccupancy = lowpulseoccupancy+duration;
  cur_time=millis();
    if(cur_time-pre_time>=sampletime_flame){
      if(flame==0&&gas>=200){
    tone(buzzer_pin,300,2000);
    pre_time=cur_time;
      }
    }
    
 //10초마다 가스 데이터를 라즈베리파이로 넘김. 10초로 설정되어있음
  if ((millis()-starttime) > sampletime_ms){//if the sampel time == 30s
    ratio = lowpulseoccupancy/(sampletime_ms*10.0); // Integer percentage 0=>100
    concentration = 1.1*pow(ratio,3)-3.8*pow(ratio,2)+520*ratio+0.62; // using spec sheet curve
        ugm3 = concentration*100/13000;
        
        //Serial.println(" ug/m3 [PM1.0]");
    lowpulseoccupancy = 0;
    if(res==0){
      //아래 serial코드 한줄이 라즈베리파이로 10초마다 미세먼지+가스+온습도 센서 보내는 코드
        Serial.println(String(ugm3)+" "+String(gas)+" "+String(temp)+" "+String(humi));
    }
        //10초주기를 갱신하기 위한 코드
    starttime = millis();
 
    if(ugm3>=30){
      digitalWrite(blue_pin,0);
      digitalWrite(red_pin,1);
    }
    else if(ugm3<30){
      digitalWrite(red_pin,0);
      digitalWrite(blue_pin,1);
    }
  }
}
