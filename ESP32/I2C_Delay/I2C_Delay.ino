#include <OneWire.h>
#include <DallasTemperature.h>
 
// Data wire is plugged into pin 2 on the Arduino
#define ONE_WIRE_BUS 23
 
// Setup a oneWire instance to communicate with any OneWire devices 
// (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);
 
// Pass our one wire reference to Dallas Temperature.
DallasTemperature sensors(&oneWire);
 
void setup(void)
{
  // start serial port
  Serial.begin(57600);
  Serial.println("Dallas Temperature IC Control Library Demo");

  // Start up the library
  sensors.begin();
}

long oldTime = millis();
long currentTime = 0;
long difference = 0;

inline int updateDifference()
{
  currentTime = millis();
  difference = currentTime - oldTime;
  oldTime = currentTime;
  return difference;
}

float dallas(int x,byte start){
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
        if (start) delay(1000);
    } while (start--);
    return temperature;
}

#define STATE 1
 
void loop(void)
{
  if (STATE == 0)
  {
    // call sensors.requestTemperatures() to issue a global temperature
    // request to all devices on the bus
    sensors.requestTemperatures(); // Send the command to get temperatures
    Serial.print("After Request: ");
    Serial.println(updateDifference());
  
    sensors.getTempCByIndex(0);
    Serial.print("After Index 0: ");
    Serial.println(updateDifference());
    
    sensors.getTempCByIndex(1);
    Serial.print("After Index 1: ");
    Serial.println(updateDifference());
  
    sensors.getTempCByIndex(2);
    Serial.print("After Index 2: ");
    Serial.println(updateDifference());
  
    sensors.getTempCByIndex(2);
    Serial.print("After Index 3: ");
    Serial.println(updateDifference());
    // Why "byIndex"? 
     // You can have more than one IC on the same bus. 
      // 0 refers to the first IC on the wire
  }
  else if (STATE == 1)
  {
    float currentTemp = dallas(23,0);
    Serial.print("After Brute Force: ");
    updateDifference();
    currentTemp = dallas(23,0);
    Serial.println(updateDifference());
  }
 
}
