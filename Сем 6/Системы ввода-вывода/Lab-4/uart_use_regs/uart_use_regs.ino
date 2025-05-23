#define SetBit(reg, bita) reg |= (1<<bita)

volatile char b = 0;

  void setup() {
    uint16_t baudRate = 38400;

    // Рассчитываем значение UBRR
    uint16_t ubrr = 16000000UL / 16 / baudRate - 1;
    if ((16000000UL / 16 / baudRate - 1) % 10 >= 5) {
        ubrr += 1;
    }

    // Записываем в регистры скорости
    UBRR0H = (unsigned char)(ubrr >> 8);
    UBRR0L = (unsigned char)ubrr;

    // Включаем передатчик, приёмник и прерывание при приёме
    SetBit(UCSR0B, TXEN0);
    SetBit(UCSR0B, RXEN0);
    SetBit(UCSR0B, RXCIE0);

    // Формат кадра: 8 бит данных
    SetBit(UCSR0C, UCSZ01);
    SetBit(UCSR0C, UCSZ00);

    // Устанавливаем odd parity: UPM01=1, UPM00=1
    SetBit(UCSR0C, UPM01);
    SetBit(UCSR0C, UPM00);

    // Устанавливаем 2 стоп-бита: USBS0=1
    SetBit(UCSR0C, USBS0);

    // На встроенный светодиод (D13)
    //pinMode(13, OUTPUT);
}

}

ISR(USART_RX_vect) {
  b = UDR0;
  
  if(b == 'A') digitalWrite(13, HIGH);
  if(b == 'B') digitalWrite(13, LOW);

  while(!(UCSR0A & (1<<UDRE0)));

  UDR0 = b;
}

void loop() {

}