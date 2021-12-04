#include <Adafruit_DS3502.h>

Adafruit_DS3502 ds3502 = Adafruit_DS3502();
/* For this example, make the following connections:
    * DS3502 RH to 5V
    * DS3502 RL to GND
    * DS3502 RW to the pin specified by WIPER_VALUE_PIN
*/

#define WIPER_VALUE_PIN 36

void setup() {
  Serial.begin(115200);
  // Wait until serial port is opened
  while (!Serial) { delay(1); }

  Serial.println("Adafruit DS3502 Test");

  if (!ds3502.begin()) {
    Serial.println("Couldn't find DS3502 chip");
    while (1);
  }
  Serial.println("Found DS3502 chip");
  ds3502.setWiperDefault(0);
}

int wiper_input = 0;
float previous_value = 0;
float wiper_value = 0;
int count = 0;

#define AVERAGE_COUNT 100
float average = 0;

#define STATE 0

bool up = true;

void loop() {
  
  if (STATE == 0) {
    ds3502.setWiper(wiper_input);
    for (int i = 0; i < AVERAGE_COUNT; i++) {
      wiper_value = analogRead(WIPER_VALUE_PIN);
      average = (average * (AVERAGE_COUNT - 1) + wiper_value / AVERAGE_COUNT);
      delay(0.05/AVERAGE_COUNT);
    }
    Serial.print(wiper_input);
    Serial.print(": ");
    Serial.println(wiper_value / 3750 * 2.848);
  

    if (wiper_input <= 0) {
      up = true;
      delay(1000);
    }

    if (up) {
        wiper_input++;
    } else {
        wiper_input--;
    }

    if (wiper_input >= 128) {
      delay(1000);
      up = false;
    }
  }

  if (STATE == 1) {
    wiper_input = 0;
    ds3502.setWiper(wiper_input);
    Serial.println(wiper_input);
    delay(3000);
    wiper_input = 63;
    ds3502.setWiper(wiper_input);
    Serial.println(wiper_input);
    delay(3000);
    wiper_input = 127;
    ds3502.setWiper(wiper_input);
    Serial.println(wiper_input);
    delay(3000);
  }

  if (STATE == 2) {
    wiper_input = 0;
    ds3502.setWiper(wiper_input);
  }
}
