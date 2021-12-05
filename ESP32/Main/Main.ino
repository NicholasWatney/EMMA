




#include <string>
#include <sstream>
#include <Adafruit_DS3502.h>

Adafruit_DS3502 ds3502 = Adafruit_DS3502();

#define LM1 36
#define LM2 39

#define LED1 32
#define LED2 33
#define LED3 25
#define LED4 26

#define DS 23

#define DS1 32
#define DS2 33
#define DS3 25
#define DS4 26

//#define WIPER_VALUE_PIN 36

#include <OneWire.h>

//int16_t dallas(int x,byte start){
//    OneWire ds(x);
//    byte i;
//    byte data[2];
//    int16_t result;
//    do{
//        ds.reset();
//        ds.write(0xCC);
//        ds.write(0xBE);
//        for (int i = 0; i < 2; i++) data[i] = ds.read();
//        result=(data[1]<<8) |data[0];
//        result>>=4; if (data[1]&128) result |=61440;
//        if (data[0]&8) ++result;
//        ds.reset();
//        ds.write(0xCC);
//        ds.write(0x44, 1);
//        if (start) delay(1000);
//    } while (start--);
//    return result;
//}


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

void inline process(std::string parsed)
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
void inline updateLM35()
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


#define TEMP_DELAY 250 // >= 750
unsigned long tempClock = 0;
void inline updateDS18B20()
{
  tempClock += timeDifference;
  if (tempClock >= TEMP_DELAY)
  {

    float DS_temp1 = dallas(DS1, 0);
    float DS_temp2 = dallas(DS2, 0);
    float DS_temp3 = dallas(DS3, 0);
    float DS_temp4 = dallas(DS4, 0);

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
void inline updateRead()
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

void inline timeClock()
{
  currentTime = millis();
  timeDifference = currentTime - oldTime;
  oldTime = currentTime;
}



void inline setupLEDs()
{
  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  pinMode(LED4, OUTPUT);
}

DeviceAddress tempDeviceAddress;
int numOfDevices;
int resolution = 12;
void inline setupSensors()
{
  sensors.begin();
  sensors.requestTemperatures();
  sensors.getAddress(tempDeviceAddress, 0); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.getAddress(tempDeviceAddress, 1); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.getAddress(tempDeviceAddress, 2); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.getAddress(tempDeviceAddress, 3); sensors.setResolution(tempDeviceAddress, resolution);
  sensors.setWaitForConversion(false);
}

void inline setupQuickSensors()
{
  dallas(DS1, 1);
  dallas(DS2, 1);
  dallas(DS3, 1);
  dallas(DS4, 1);
}

void setupDS3502()
{
  if (!ds3502.begin()) 
  {
    Serial.println("Couldn't find DS3502 chip");
  }
   ds3502.setWiperDefault(0);
}

#define DS3502_DELAY 5
unsigned long DS3502_Clock = 0;
int wiper_input = 0;
bool up = true;
void inline updateDS3502()
{
  DS3502_Clock += timeDifference;
  if (DS3502_Clock >= DS3502_DELAY)
  {
    ds3502.setWiper(wiper_input);
    if (wiper_input <= 0) 
    {
      up = true;
    }
  
    if (up) {
        wiper_input++;
    } else {
        wiper_input--;
    }
  
    if (wiper_input >= 128) 
    {
      up = false;
    }
    DS3502_Clock = DS3502_Clock % DS3502_DELAY;
  }
}

void setup()
{
  Serial.begin(57600);
//  setupLEDs();
//  setupSensors();
  setupQuickSensors();
  setupDS3502();
}

int bootAnswer = 0;
void inline bootListen() {
  while (true) {
    addMessage("BL:1");
    messageFlush();
    updateRead();
    if (bootAnswer == 1) {
      bootAnswer = 0;
      break;
    }
  }
}

void loop()
{
  //Listen for charging protocol
  

  // Execute main code
  while (true) {
  timeClock();
    updateDS18B20();
    updateDS3502();
    messageFlush();
    updateRead();

  }
}
