#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Музыкальная клавиатура STM32 - Клиент для управления через UART
"""

import serial
import serial.tools.list_ports
import sys
import time
import threading

class MusicalKeyboard:
    def __init__(self, port=None, baudrate=115200):
        """
        Инициализация музыкальной клавиатуры

        Args:
            port: COM-порт (например, 'COM3' или '/dev/ttyUSB0')
            baudrate: Скорость передачи данных (по умолчанию 115200)
        """
        self.port = port
        self.baudrate = baudrate
        self.serial_conn = None
        self.running = False

    def list_ports(self):
        """Список доступных COM-портов"""
        ports = serial.tools.list_ports.comports()
        print("\nДоступные COM-порты:")
        print("-" * 50)
        for i, port in enumerate(ports, 1):
            print(f"{i}. {port.device} - {port.description}")
        print("-" * 50)
        return ports

    def connect(self):
        """Подключение к микроконтроллеру"""
        try:
            if not self.port:
                ports = self.list_ports()
                if not ports:
                    print("Не найдено доступных COM-портов!")
                    return False

                choice = input("\nВыберите номер порта (или введите имя порта): ").strip()

                if choice.isdigit():
                    idx = int(choice) - 1
                    if 0 <= idx < len(ports):
                        self.port = ports[idx].device
                    else:
                        print("Неверный номер порта!")
                        return False
                else:
                    self.port = choice

            print(f"\nПодключение к {self.port}")
            self.serial_conn = serial.Serial(
                port=self.port,
                baudrate=self.baudrate,
                bytesize=serial.EIGHTBITS,
                parity=serial.PARITY_NONE,
                stopbits=serial.STOPBITS_ONE,
                timeout=1
            )

            time.sleep(2)  # Ждем инициализации микроконтроллера

            print("Подключение установлено!")
            self.running = True

            # Запуск потока для чтения данных
            self.read_thread = threading.Thread(target=self._read_loop, daemon=True)
            self.read_thread.start()

            return True

        except serial.SerialException as e:
            print(f"Ошибка подключения: {e}")
            return False

    def disconnect(self):
        """Отключение от микроконтроллера"""
        self.running = False
        if self.serial_conn and self.serial_conn.is_open:
            self.serial_conn.close()
            print("\nОтключено от микроконтроллера")

    def _read_loop(self):
        """Поток для чтения данных от микроконтроллера"""
        while self.running:
            try:
                if self.serial_conn and self.serial_conn.in_waiting > 0:
                    data = self.serial_conn.readline()
                    try:
                        message = data.decode('utf-8', errors='ignore').strip()
                        if message:
                            print(f"\nSTM32: {message}")
                            print(">>> ", end='', flush=True)
                    except UnicodeDecodeError:
                        pass
            except Exception as e:
                if self.running:
                    print(f"\nОшибка чтения: {e}")
            time.sleep(0.01)

    def send_command(self, command):
        """
        Отправка команды на микроконтроллер

        Args:
            command: Команда для отправки
        """
        try:
            if self.serial_conn and self.serial_conn.is_open:
                self.serial_conn.write(command.encode('utf-8'))
                self.serial_conn.flush()
                return True
            else:
                print("Нет подключения к микроконтроллеру!")
                return False
        except Exception as e:
            print(f"Ошибка отправки: {e}")
            return False

    def play_note(self, note_number):
        """Воспроизвести ноту (1-7)"""
        if 1 <= note_number <= 7:
            self.send_command(str(note_number))
        else:
            print("Номер ноты должен быть от 1 до 7!")

    def octave_up(self):
        """Увеличить октаву"""
        self.send_command('+')

    def octave_down(self):
        """Уменьшить октаву"""
        self.send_command('-')

    def duration_up(self):
        """Увеличить длительность"""
        self.send_command('A')

    def duration_down(self):
        """Уменьшить длительность"""
        self.send_command('a')

    def play_scale(self):
        """Воспроизвести гамму"""
        self.send_command('\r')

    def show_help(self):
        """Показать справку"""
        print("\n" + "=" * 60)
        print("Клава")
        print("=" * 60)
        print("\nКоманды:")
        print("  1-7      : Воспроизвести ноту (До, Ре, Ми, Фа, Соль, Ля, Си)")
        print("  +        : Увеличить октаву")
        print("  -        : Уменьшить октаву")
        print("  A        : Увеличить длительность на 0.1с")
        print("  a        : Уменьшить длительность на 0.1с")
        print("  enter    : Воспроизвести все ноты октавы")
        print("  help     : Показать эту справку")
        print("  quit     : Выход")
        print("=" * 60 + "\n")

    def play_melody(self, melody_name):
        """Воспроизвести предустановленную мелодию"""
        melodies = {
            'c_major': '1234567',  # Гамма до-мажор
            'happy_birthday': '112312112342',
            'twinkle': '1155665',
            'test': '1234321',
        }

        if melody_name in melodies:
            notes = melodies[melody_name]
            for note in notes:
                self.send_command(note)
                time.sleep(0.5)
        else:
            print(f"Мелодия '{melody_name}' не найдена!")

    def interactive_mode(self):
        """Интерактивный режим управления"""
        self.show_help()

        try:
            while self.running:
                try:
                    command = input(">>> ").strip()

                    if not command:
                        continue

                    # Обработка спецкоманд
                    if command.lower() in ['quit', 'exit', 'q']:
                        break
                    elif command.lower() == 'help':
                        self.show_help()
                    elif command.lower().startswith('melody '):
                        melody_num = command.split()[1]
                        self.play_melody(melody_num)
                    elif command.lower() in ['scale', 'enter']:
                        self.play_scale()
                    else:
                        # Отправка команды посимвольно
                        for char in command:
                            if char == ' ':
                                time.sleep(0.3)  # Пауза между нотами
                            else:
                                self.send_command(char)
                                time.sleep(0.05)

                except KeyboardInterrupt:
                    print("\n\nПрервано пользователем")
                    break
                except EOFError:
                    break

        finally:
            self.disconnect()


def main():
    keyboard = MusicalKeyboard()

    # Подключение
    if keyboard.connect():
        # Запуск интерактивного режима
        keyboard.interactive_mode()
    else:
        print("\nНе удалось подключиться к микроконтроллеру!")
        sys.exit(1)

    print("\nДо свидания!")


if __name__ == "__main__":
    main()
