#include <Adafruit_BMP280.h>

#define BMP_SCK   (13)
#define BMP_MISO  (12)
#define BMP_MOSI  (11)
#define BMP_CS    (10)
#define SetBit(reg, bita) reg |= (1<<bita)

Adafruit_BMP280 bmp(BMP_CS); // hardware SPI

#define SYNC_BYTE 0x5A
#define READ_BYTE 0x5B
#define MAX_PACKET_DATA 64

typedef enum {
  WAIT_START = 0,
  WAIT_LEN,
  WAIT_DATA,
  WAIT_CRC
} PacketState;

PacketState packet_state = WAIT_START;
uint8_t packet_length = 0;
uint8_t data_index = 0;
uint8_t packet_data[MAX_PACKET_DATA];
uint8_t received_crc = 0;
uint8_t packet_ready = 0;

String custom_message = ""; // Переменная, которая хранит пользовательское сообщение

uint8_t crc8(String data) {
  uint8_t crc = 0x00;
  const uint8_t polynomial = 0x07;
  for (size_t i = 0; i < data.length(); i++) {
    crc ^= data[i];
    for (uint8_t j = 0; j < 8; j++) {
      if (crc & 0x80) {
        crc = (crc << 1) ^ polynomial;
      } else {
        crc <<= 1;
      }
    }
  }
  return crc;
}

void serialWriteByte(uint8_t byte) {
  while (!(UCSR0A & (1 << UDRE0))); // Ждём, пока буфер пуст
  UDR0 = byte;
}

void serialWriteString(String data) {
  for (size_t i = 0; i < data.length(); i++) {
    serialWriteByte(data[i]);
  }
}

void serial_send_packet(String data) {
  if (data.length() > 0xFF) return;

  serialWriteByte(SYNC_BYTE);
  serialWriteByte(data.length());
  serialWriteString(data);
  serialWriteByte(crc8(data));
}

String dataToString(uint8_t* data, uint8_t length) {
  String result = "";
  for (uint8_t i = 0; i < length; i++) {
    result += (char)data[i];
  }
  return result;
}

void debugPrint(String msg) {
  serial_send_packet("[DEBUG] " + msg + "\n");
}

void processSerial() {
  while (Serial.available()) {
    uint8_t b = UDR0;
    switch (packet_state) {
      case WAIT_START:
        if (b == SYNC_BYTE) {
          packet_state = WAIT_LEN;
        } else if (b == READ_BYTE) {
          serial_send_packet("DEBUUUUUUUG");
        }
        break;

      case WAIT_LEN:
        packet_length = b;
        data_index = 0;
        if (packet_length == 0)
          packet_state = WAIT_START;
        else
          packet_state = WAIT_DATA;
        break;

      case WAIT_DATA:
        if (data_index < MAX_PACKET_DATA) {
          packet_data[data_index++] = b;
          if (data_index >= packet_length) {
            packet_state = WAIT_CRC;
          }
        } else {
          packet_state = WAIT_START; // too much data
        }
        break;

      case WAIT_CRC:
        received_crc = b;
        if (crc8(dataToString(packet_data, packet_length)) == received_crc) {
          custom_message = dataToString(packet_data, packet_length);
          packet_ready = 1;
        }
        packet_state = WAIT_START;
        break;

      default:
        packet_state = WAIT_START;
        break;
    }
  }
}

void serialSetup() {
  uint16_t baudRate = 38400;
  uint16_t ubrr = 16000000 / 16 / baudRate - 1;

  UBRR0H = (unsigned char)(ubrr >> 8);
  UBRR0L = (unsigned char)ubrr;

  SetBit(UCSR0B, TXEN0);
  SetBit(UCSR0B, RXEN0);

  UCSR0C = (1 << UPM01) | (0 << UPM00) |
          (1 << USBS0) |
          (1 << UCSZ01) | (1 << UCSZ00);
}

void setup() {
  serialSetup();
  bmp.begin();

  bmp.setSampling(Adafruit_BMP280::MODE_NORMAL,
                  Adafruit_BMP280::SAMPLING_X2,
                  Adafruit_BMP280::SAMPLING_X16,
                  Adafruit_BMP280::FILTER_X16,
                  Adafruit_BMP280::STANDBY_MS_500);
}

void loop() {
  // processSerial();

  

  if (custom_message != "") {
    delay(100);
    serial_send_packet(custom_message);
    // custom_message = ""; // Clear the message after sending
  } else {
    serial_send_packet("Temperature = " + String(bmp.readTemperature()) + " *C\n");
    serial_send_packet("Pressure = " + String(bmp.readPressure()) + " Pa\n");
  }

  delay(1000);
}
