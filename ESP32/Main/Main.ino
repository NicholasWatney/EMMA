

#include <driver/gpio.h>
#include <driver/uart.h>
// Include FreeRTOS for delay
#include <freertos/FreeRTOS.h>
#include <freertos/task.h>

#define LED1 32
#define LED2 33
#define LED3 25
#define LED4 26

#define LM1 36
#define LM2 39
#define LM3 34

#define DELAY 10
#define LEDDELAY 1000
#define LMDELAY 100

void setupLM();
void setupLED();
void setup();
void loopLED();
void loopLM();
void loopUART();
void displayLM();
void loop();

using namespace std;

void setupLM()
{
}

void setupLED()
{
  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  pinMode(LED4, OUTPUT);
}

const uart_port_t uart_num = UART_NUM_1;
void setupUART()
{
  uart_config_t uart_config = {
    .baud_rate = 115200,
    .data_bits = UART_DATA_8_BITS,
    .parity = UART_PARITY_DISABLE,
    .stop_bits = UART_STOP_BITS_1,
    .flow_ctrl = UART_HW_FLOWCTRL_CTS_RTS,
    .rx_flow_ctrl_thresh = 122,
  };
  // Configure UART parameters
  ESP_ERROR_CHECK(uart_param_config(uart_num, &uart_config));
  // Set UART pins(TX: 1, RX: 3, RTS: 22, CTS: 21)
  ESP_ERROR_CHECK(uart_set_pin(UART_NUM_1, 1, 3, 22, 21));

  // Setup UART buffered IO with event queue
  const int uart_buffer_size = (1024 * 2);
  QueueHandle_t uart_queue;
  // Install UART driver using an event queue here
  ESP_ERROR_CHECK(uart_driver_install(UART_NUM_1, uart_buffer_size, \
                                          uart_buffer_size, 10, &uart_queue, 0)); 
}

void setup() {
  Serial.begin(115200);
  setupLED();
  setupLM();
//  setupUART();
}

unsigned int LEDCount = 0;
bool LEDState = 0;
void loopLED()
{

  if (LEDCount >= LEDDELAY)
  {
    
    LEDState = !LEDState;
    LEDCount = LEDCount % LEDDELAY;

    switch (LEDState)
    {
      case 0:
        digitalWrite(LED1, LOW);
        digitalWrite(LED2, LOW);
        digitalWrite(LED3, LOW);
        digitalWrite(LED4, LOW);
        break;
      case 1:
        digitalWrite(LED1, HIGH);
        digitalWrite(LED2, HIGH);
        digitalWrite(LED3, HIGH);
        digitalWrite(LED4, HIGH);
        break;
    }
  }
  
  LEDCount += DELAY;
}

unsigned int LMCount = 0;
float temp1;
float temp2;
float temp3;

void loopLM()
{
  if (LMCount > LMDELAY)
  {
    int reading1 = analogRead(LM1);
    int reading2 = analogRead(LM2);
    int reading3 = analogRead(LM3);
    temp1 = reading1 / 5000.0 * 155;
    temp2 = reading2 / 5000.0 * 155;
    temp3 = reading3 / 5000.0 * 155;
    displayLM();
    LMCount = LMCount % LMDELAY;
  }

  LMCount += DELAY;
}

unsigned int UARTCount = 0;
void sendUART()
{
  char test_str[100];
  sprintf(test_str, "This is a test string: %d\n", UARTCount);
  uart_write_bytes(uart_num, (const char*)test_str, strlen(test_str));
  UARTCount++;
}

void receiveUART()
{
  // Read data from UART.
  const uart_port_t uart_num = UART_NUM_1;
  uint8_t data[128];
  int length = 0;
  ESP_ERROR_CHECK(uart_get_buffered_data_len(uart_num, (size_t*)&length));
  length = uart_read_bytes(uart_num, data, length, 100);
}

void loopUART()
{
//  sendUART();
//  receiveUART();
}

void displayLM()
{
  Serial.print(temp1);
  Serial.print(" \xC2\xB0 C ");
  Serial.print(temp2);
  Serial.print(" \xC2\xB0 C ");
  Serial.print(temp3);
  Serial.println(" \xC2\xB0 C");
}

void loop() {
  loopLED();
  loopLM();
  loopUART();
  delay(DELAY);
}
