

#define LED1 32
#define LED2 33
#define LED3 25
#define LED4 26

#define LM1 36
#define LM2 39
#define LM3 34

void setup() {
  Serial.begin(57600);
  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  pinMode(LED4, OUTPUT);
}

boolean led1 = false;

#include <string>

void loop()
{
  int reading1 = analogRead(LM1);
  float temp1 = reading1 / 5000.0 * 155; 
  int reading2 = analogRead(LM2);
  float temp2 = reading2 / 5000.0 * 155;
  int reading3 = analogRead(LM3);
  float temp3 = reading3 / 5000.0 * 155;
  Serial.print("T1:");
  Serial.print(temp1);
  Serial.print(";\n");
  Serial.print("T2:");
  Serial.print(temp2);
  Serial.print(";\n");
  Serial.print("T3:");
  Serial.print(temp3);
  Serial.print(";\n");
}
