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

uint8_t packet_length = 0;
uint8_t data_index = 0;
uint8_t packet_data[MAX_PACKET_DATA];
uint8_t received_crc = 0;
uint8_t packet_ready = 0;

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

int stage = 0;
uint8_t length = 0;
uint8_t c = 0;

void loop() {
  while (!(UCSR0A & (1<<RXC0))) {
    // if (stage == 0) {
      // serial_send_packet("Temperature = " + String(bmp.readTemperature()) + " *C\n");
      // serial_send_packet("Pressure = " + String(bmp.readPressure()) + " Pa\n");
      // delay(1000);
    // }
  }
  uint8_t byte = UDR0;
  switch (stage) {
    case 0: if (byte == SYNC_BYTE) stage = 1; serial_send_packet("Byte1 = " + String(byte) + ".\n"); break;
    case 1: stage = 2; length = byte; serial_send_packet("Byte2 = " + String(byte) + ".\n"); c = 0; break;
    case 2: {
      if (c < length) {
        serial_send_packet("Byte3 = " + String(byte) + ".\n");
        c += 1;
      } else {
        stage = 3;
      }
      break;
    }
    case 3: serial_send_packet("Byte4 = " + String(byte) + ".\n"); stage = 0; break;
  }
  while (!(UCSR0A & (1<<UDRE0)));
  // delay(500);
  // uint8_t length = UDR0;)
  // serial_send_packet("Byte = " + String(synchrobyte) + ".\n");
  // serial_send_packet("Byte2 = " + String(length) + ".\n");
  // for (int i = 0; i < length; i++) {
  //   uint8_t cur_byte = UDR0;
  //   serial_send_packet("Byte3 = " + String(cur_byte) + ".\n");
  // }
  // uint8_t crc = UDR0;
  // serial_send_packet("Byte6 = " + String(crc) + ".\n");
  // if  (crc == calculate_crc(bytes)) {
  //   serial_send_packet(...bytes...)
  // }
  // }
}
