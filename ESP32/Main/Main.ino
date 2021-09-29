
#define LED1 32
#define LED2 33
#define LED3 25
#define LED4 26

#define LM1 36
#define LM2 39
#define LM3 34

#include <string>
#include <sstream>

void setup()
{
  Serial.begin(57600);
  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  pinMode(LED4, OUTPUT);
}


std::string parsed = "";

boolean led1 = false;
boolean led2 = false;
boolean led3 = false;
boolean led4 = false;

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

template<class T>
std::string toString(const T &value) {
    std::ostringstream os;
    os << value;
    return os.str();
}

void loop()
{
  
  int reading1 = analogRead(LM1);
  float temp1 = reading1 / 5000.0 * 155; 
  int reading2 = analogRead(LM2);
  float temp2 = reading2 / 5000.0 * 155;
  int reading3 = analogRead(LM3);
  float temp3 = reading3 / 5000.0 * 155;
  std::string message = "";
  message += "T1:" + toString(temp1) + toString(";");
  message += "T2:" + toString(temp2) + toString(";");
  message += "T3:" + toString(temp3) + toString(";");
  char charMessage[message.size()+1];//as 1 char space for null is also required
  strcpy(charMessage, message.c_str());
  Serial.write(charMessage);

  if (Serial.available() > 0)
  {
    char character = (char) Serial.read();
    parsed = parsed + character;  
    if (character == ';')
    {
      process(parsed);
      parsed = "";
    }
  }
}
