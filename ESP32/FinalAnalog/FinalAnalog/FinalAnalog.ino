

#include <Adafruit_DS3502.h>
Adafruit_DS3502 ds3502 = Adafruit_DS3502();

#define ACK 14
#define B1 39
#define B2 34
#define B3 35

#define SAFETY_DISCHARGE_RELAY 17
#define TP_CONTACTOR 19
#define POWER_DECK_SOLENOID 18
#define POWER_DECK_ENABLE 23

boolean volatile ack = false;
boolean volatile bit1 = false;
boolean volatile bit2 = false;
boolean volatile bit3 = false;

boolean volatile receivingBits = false;
long volatile startTime = -1;


void IRAM_ATTR B1Triggered() {
  if (receivingBits == false) {
    receivingBits = true;
  }
  bit1 = true;
  Serial.println("B1");
}

void IRAM_ATTR B2Triggered() {
  if (receivingBits == false) {
    receivingBits = true;
  }
  bit2 = true;
  Serial.println("B2");
}
  
void IRAM_ATTR B3Triggered() {
  if (receivingBits == false) {
    receivingBits = true;
  }
  bit3 = true;
  Serial.println("B3");
}

void setupInterrupts()
{
  pinMode(ACK, OUTPUT);
  pinMode(B1, INPUT);
  pinMode(B2, INPUT);
  pinMode(B3, INPUT);
  
  attachInterrupt(B1, B1Triggered, RISING); 
  attachInterrupt(B2, B2Triggered, RISING); 
  attachInterrupt(B3, B3Triggered, RISING);  
}

void setupPins()
{
  pinMode(SAFETY_DISCHARGE_RELAY, OUTPUT);
  pinMode(TP_CONTACTOR, OUTPUT);
  pinMode(POWER_DECK_SOLENOID, OUTPUT);
  pinMode(POWER_DECK_ENABLE, OUTPUT);

  digitalWrite(SAFETY_DISCHARGE_RELAY, HIGH);
  digitalWrite(TP_CONTACTOR, HIGH);
  digitalWrite(POWER_DECK_SOLENOID, HIGH);
  digitalWrite(POWER_DECK_ENABLE, LOW);
}

boolean DS3502_alive;
void setupDS3502()
{
  if (!ds3502.begin()) 
  {
    DS3502_alive = false;
    return;
  } else {
    DS3502_alive = true;
    ds3502.setWiperDefault(0);
    return;
  }
}

void setup() {
  Serial.begin(57600);
  setupInterrupts();
  setupDS3502();
  setupPins();
}

#define TIME 50

int interpretInput() {
  if (receivingBits == false) {
    return -1;
  } else {
    if (startTime == -1) {
      startTime = millis();
      return -1;
    } else {
      if ((millis() - startTime) > TIME) {
        int actionValue;
        if (bit3 == 0 && bit2 == 0 && bit1 == 1) {
          Serial.println("1");
          actionValue = 1;
        } else if (bit3 == 0 && bit2 == 1 && bit1 == 0) {
          Serial.println("2");
          actionValue = 2;
        } else if (bit3 == 0 && bit2 == 1 && bit1 == 1) {
          Serial.println("3");
          actionValue = 3;
        } else if (bit3 == 1 && bit2 == 0 && bit1 == 0) {
          Serial.println("4");
          actionValue = 4;
        } else if (bit3 == 1 && bit2 == 0 && bit1 == 1) {
          Serial.println("5");
          actionValue = 5;
        } else if (bit3 == 1 && bit2 == 1 && bit1 == 0) {
          Serial.println("6");
          actionValue = 6;
        } else if (bit3 == 1 && bit2 == 1 && bit1 == 1) {
          Serial.println("7");
          actionValue = 7;
        }
        bit1 = 0; bit2 = 0; bit3 = 0;
        digitalWrite(ACK, HIGH);
        delay(200);
        digitalWrite(ACK, LOW);; // short time stamp
        delay(25);
        receivingBits = false;;
        startTime = -1;
        return actionValue;
      } else {
        return -1;
      }
    }
  }
}

void inline chargeUp() {
  digitalWrite(SAFETY_DISCHARGE_RELAY, HIGH);
  delay(5000);
  digitalWrite(TP_CONTACTOR, LOW);
  delay(5000);
  digitalWrite(POWER_DECK_SOLENOID, LOW);
  delay(5000);
  return;
}

void inline discharge() {
  digitalWrite(POWER_DECK_ENABLE, LOW);
  delay(5000);
  digitalWrite(TP_CONTACTOR, HIGH);
  delay(5000);
  digitalWrite(POWER_DECK_SOLENOID, HIGH);
  delay(5000);
  digitalWrite(SAFETY_DISCHARGE_RELAY, LOW);
  delay(5000);
  digitalWrite(SAFETY_DISCHARGE_RELAY, HIGH);
  delay(5000);
  return;
}

void inline mainApp() {}

#define CHARGEUP_ACTION 1

#define DISCHARGE_ACTION 1

#define START_ACTION 1
#define STOP_ACTION 2
#define FREQUENCY_UP_ACTION 3
#define FREQUENCY_DOWN_ACTION 4
#define SHUTDOWN_ACTION 5


int wiper_input = 100;

void loop() {

  while (true) {
    if (interpretInput() == CHARGEUP_ACTION) {
      
      break;
    }
  }
  Serial.println("CHARGEUP_ACTION");
  chargeUp();

  while (true) {
    int value = interpretInput();
    if (value == START_ACTION) {
      digitalWrite(POWER_DECK_ENABLE, HIGH);
    } else if (value == STOP_ACTION) {
      digitalWrite(POWER_DECK_ENABLE, LOW);
    } else if (value == FREQUENCY_UP_ACTION) {
      if (DS3502_alive == true) {
        wiper_input = wiper_input + 1;
        ds3502.setWiper(wiper_input);
        Serial.print("DS3502: ");
        Serial.println(wiper_input);
      }
    } else if (value == FREQUENCY_DOWN_ACTION) {
      if (DS3502_alive == true) {
        wiper_input = wiper_input - 1;
        ds3502.setWiper(wiper_input);
        Serial.print("DS3502: ");
        Serial.println(wiper_input);
      }
    } else if (value == SHUTDOWN_ACTION) {
      break;
    }
  }

  while (true) {
    if (interpretInput() == DISCHARGE_ACTION) {
      break;
    }
  }

  discharge();
}
