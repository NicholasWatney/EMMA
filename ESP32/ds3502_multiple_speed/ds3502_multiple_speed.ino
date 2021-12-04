#include <OneWire.h>

//23
//32
//33

#define DS1 32
#define DS2 33
#define DS3 25
#define DS4 26

float inline dallas(int x, byte start){
    OneWire ds(x);
    byte i;
    byte data[2];
    int16_t result;
    float temperature;
    do{
        ds.reset();
        ds.write(0xCC);
        ds.write(0xBE);
        for (int i = 0; i < 2; i++) data[i] = ds.read();
        result=(data[1]<<8) |data[0];
        // Here you could print out the received bytes as binary, as requested in my comment:
        // Serial.println(result, BIN);
        int16_t whole_degree = (result & 0x07FF) >> 4; // cut out sign bits and shift
        temperature = whole_degree + 0.5*((data[0]&0x8)>>3) + 0.25*((data[0]&0x4)>>2) + 0.125*((data[0]&0x2)>>1) + 0.0625*(data[0]&0x1);
        if (data[1]&128) temperature*=-1;
        ds.reset();
        ds.write(0xCC);
        ds.write(0x44, 1);
        if (start) delay(10);
    } while (start--);
    return temperature;
}

void setup() {
  Serial.begin(57600);
  dallas(DS1, 1);
  dallas(DS2, 1);
  dallas(DS3, 1);
  dallas(DS4, 1);
}

void loop() {
  float DS_temp1 = dallas(DS1, 0);
  Serial.print(DS_temp1);
  Serial.print(" | ");
  float DS_temp2 = dallas(DS2, 0);
  Serial.print(DS_temp2);

  Serial.print(" | ");
  float DS_temp3 = dallas(DS3, 0);
  Serial.print(DS_temp3);

  Serial.print(" | ");
  float DS_temp4 = dallas(DS4, 0);
  Serial.println(DS_temp4);
  delay(5);
}
