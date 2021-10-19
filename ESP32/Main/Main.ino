
#define LED1 32
#define LED2 33
#define LED3 25
#define LED4 26

#define LM1 36
#define LM2 39

#define DS 23

#include <string>
#include <sstream>

#include <OneWire.h>

int16_t dallas(int x, byte start)
{
  OneWire ds(x);
  byte i;
  byte data[9];
  int16_t result;
  do
  {
    ds.reset();
    ds.write(0xCC);
    ds.write(0xBE);
    for (i = 0; i < 9; i++) data[i] = ds.read();
    result = (data[1]<<8) | data[0];
    result>>=4; if (data[i]&128) result |= 61440;
    if (data[0]&8) ++result;
    ds.reset();
    ds.write(0xCC);
    ds.write(0x44, 1);
    if (start) delay(1000);
  } while (start--);
  return result;
}

#include <DallasTemperature.h>

OneWire oneWire(DS);
DallasTemperature sensors(&oneWire);

boolean led1 = false;
boolean led2 = false;
boolean led3 = false;
boolean led4 = false;

unsigned long oldTime = -1;
unsigned long currentTime = -1;
unsigned long timeDifference = 0;
void timeClock();

std::string UARTMessage = "";
template<class T>
void inline addMessage(const T &message)
{
  UARTMessage += toString(message) + toString(";");
}

void inline messageFlush()
{
  if (UARTMessage != "")
  {
    UARTMessage += toString("\n");
    char charMessage[UARTMessage.size()+1];//as 1 char space for null is also required
    strcpy(charMessage, UARTMessage.c_str());
    Serial.write(charMessage);
    UARTMessage = "";
  }
}

template<class T>
std::string inline toString(const T &value) 
{
    std::ostringstream os;
    os << value;
    return os.str();
}

void process(std::string parsed)
{
  std::string label = parsed.substr(0, parsed.find(":"));
  if (label == "L1")
  {
    led1 = !led1;
    if (led1 == true)
    {
      digitalWrite(LED1, HIGH);
    }
    else
    {
      digitalWrite(LED1, LOW);
    }
  }

  if (label == "L2")
  {
    led2 = !led2;
    if (led2 == true)
    {
      digitalWrite(LED2, HIGH);
    }
    else
    {
      digitalWrite(LED2, LOW);
    }
  }

  if (label == "L3")
  {
    led3 = !led3;
    if (led3 == true)
    {
      digitalWrite(LED3, HIGH);
    }
    else
    {
      digitalWrite(LED3, LOW);
    }
  }

  
  if (label == "L4")
  {
    led4 = !led4;
    if (led4 == true)
    {
      digitalWrite(LED4, HIGH);
    }
    else
    {
      digitalWrite(LED4, LOW);
    }
  }
}

#define LM35_DELAY 50
unsigned long clockLM35 = 0;
void updateLM35()
{
  clockLM35 += timeDifference;
  if (clockLM35 >= LM35_DELAY)
  {
    int reading1 = analogRead(LM1);
    float temp1 = reading1 / 5000.0 * 155; 
    int reading2 = analogRead(LM2);
    float temp2 = reading2 / 5000.0 * 155;
    addMessage("T1:" + toString(temp1));
    addMessage("T2:" + toString(temp2));
    clockLM35 = clockLM35 % LM35_DELAY;
  }
}

#define TEMP_DELAY 750 // >= 750
unsigned long tempClock = 0;
void updateDS18B20()
{
  tempClock += timeDifference;
  if (tempClock >= TEMP_DELAY)
  {
    float DS_temp1 = sensors.getTempCByIndex(0);
    float DS_temp2 = sensors.getTempCByIndex(1);
    float DS_temp3 = sensors.getTempCByIndex(2);
    float DS_temp4 = sensors.getTempCByIndex(3);
    
    sensors.requestTemperatures();
    addMessage("T1:" + toString(DS_temp1));
    addMessage("T2:" + toString(DS_temp2));
    addMessage("T3:" + toString(DS_temp3));
    addMessage("T4:" + toString(DS_temp4));
    tempClock = tempClock % TEMP_DELAY;
  }
}

#define READ_DELAY 100
std::string parsed = "";
unsigned long readClock = 0;
void updateRead()
{
  readClock += timeDifference;
  if (readClock % READ_DELAY == 0)
  {
    while (Serial.available() > 0)
    {
      char character = (char) Serial.read();
      parsed = parsed + character;  
      if (character == ';')
      {
        process(parsed);
        parsed = "";
      }
    }
    readClock = readClock % READ_DELAY;
  }
}

void timeClock()
{
  currentTime = millis();
  timeDifference = currentTime - oldTime;
  oldTime = currentTime;
}

DeviceAddress tempDeviceAddress;
int numOfDevices;
int resolution = 12;
void setup()
{
  Serial.begin(57600);
  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  pinMode(LED4, OUTPUT);
  

  sensors.begin();
  sensors.requestTemperatures();
  sensors.getAddress(tempDeviceAddress, 0); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.getAddress(tempDeviceAddress, 1); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.getAddress(tempDeviceAddress, 2); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.getAddress(tempDeviceAddress, 3); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.setWaitForConversion(false);
}

void loop()
{
  timeClock();
  updateDS18B20();
  messageFlush();
  updateRead();
}
