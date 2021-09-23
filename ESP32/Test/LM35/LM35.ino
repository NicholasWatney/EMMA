

#define LM1 36
#define LM2 39
#define LM3 34

void setup() {
  Serial.begin(9600);
}

void loop() {
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
  delay(1000);
}
