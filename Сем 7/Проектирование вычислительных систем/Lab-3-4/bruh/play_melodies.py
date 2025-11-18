#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Примеры мелодий для музыкальной клавиатуры STM32
"""

import serial
import time
import sys

class MelodyPlayer:
    def __init__(self, port='COM3', baudrate=115200):
        """Инициализация проигрывателя мелодий"""
        self.port = port
        self.baudrate = baudrate
        self.ser = None

    def connect(self):
        """Подключение к микроконтроллеру"""
        try:
            self.ser = serial.Serial(self.port, self.baudrate, timeout=1)
            time.sleep(2)  # Ждем инициализации
            print(f"✅ Подключено к {self.port}")
            return True
        except Exception as e:
            print(f"❌ Ошибка подключения: {e}")
            return False

    def disconnect(self):
        """Отключение"""
        if self.ser and self.ser.is_open:
            self.ser.close()
            print("🔌 Отключено")

    def play_sequence(self, sequence, default_delay=0.3):
        """
        Воспроизвести последовательность команд

        Формат последовательности:
        - '1'-'7': ноты
        - '+'/'-': октава
        - 'A'/'a': длительность
        - ' ': пауза (default_delay)
        - '|': длинная пауза (default_delay * 2)
        - цифра после ':': задержка в секундах (например, '1:0.5')
        """
        print(f"\n🎵 Воспроизведение...")

        i = 0
        while i < len(sequence):
            char = sequence[i]

            if char == ' ':
                time.sleep(default_delay)
            elif char == '|':
                time.sleep(default_delay * 2)
            elif char == ':' and i > 0:
                # Специальная задержка
                i += 1
                delay_str = ''
                while i < len(sequence) and (sequence[i].isdigit() or sequence[i] == '.'):
                    delay_str += sequence[i]
                    i += 1
                time.sleep(float(delay_str))
                continue
            elif char == '\n':
                self.ser.write(b'\r')  # Enter
                time.sleep(default_delay)
            else:
                self.ser.write(char.encode())
                time.sleep(default_delay * 0.3)

            i += 1

        print("✓ Завершено")

    def play_melody(self, name, melody_data):
        """Воспроизвести именованную мелодию"""
        print(f"\n{'='*60}")
        print(f"🎼 {name}")
        print(f"{'='*60}")

        sequence = melody_data['sequence']
        delay = melody_data.get('delay', 0.3)

        # Установка начальной октавы и длительности
        if 'octave' in melody_data:
            octave_changes = melody_data['octave']
            if octave_changes > 0:
                self.ser.write(b'+' * octave_changes)
            elif octave_changes < 0:
                self.ser.write(b'-' * abs(octave_changes))
            time.sleep(0.2)

        if 'duration' in melody_data:
            dur_changes = melody_data['duration']
            if dur_changes > 0:
                self.ser.write(b'A' * dur_changes)
            elif dur_changes < 0:
                self.ser.write(b'a' * abs(dur_changes))
            time.sleep(0.2)

        # Воспроизведение
        self.play_sequence(sequence, delay)


# Коллекция мелодий
MELODIES = {
    'scale': {
        'sequence': '1234567',
        'delay': 0.4,
        'description': 'Гамма до-мажор'
    },

    'scale_up_down': {
        'sequence': '1234567 7654321',
        'delay': 0.3,
        'description': 'Гамма вверх и вниз'
    },

    'happy_birthday': {
        'sequence': '112:0.5 3:0.3 12 112:0.5 4:0.3 3 117:0.5 6:0.3 5 555:0.5 4:0.3 32 666:0.5 5:0.3 4',
        'delay': 0.25,
        'description': 'Happy Birthday'
    },

    'twinkle': {
        'sequence': '1 1 5 5 6 6 5| 4 4 3 3 2 2 1',
        'delay': 0.4,
        'description': 'Twinkle Twinkle Little Star (начало)'
    },

    'ode_to_joy': {
        'sequence': '3 3 4 5 5 4 3 2 1 1 2 3 3:0.5 2:0.2 2',
        'delay': 0.35,
        'description': 'Ода к радости (Бетховен) - начало'
    },

    'jingle_bells': {
        'sequence': '3 3 3| 3 3 3| 3 5 1 2 3',
        'delay': 0.3,
        'description': 'Jingle Bells (начало)'
    },

    'mario': {
        'sequence': '3 3| 3| 1 3| 5||| 5:1',
        'delay': 0.2,
        'octave': 1,  # Начать с октавы выше
        'description': 'Super Mario Bros (начальная тема)'
    },

    'tetris': {
        'sequence': '3 1 2 3 2 1 7 1 2 3',
        'delay': 0.25,
        'description': 'Тетрис (начало темы)'
    },

    'imperial_march': {
        'sequence': '1 1 1 5:0.6| 1+1+1+ 5| 1',
        'delay': 0.35,
        'description': 'Imperial March (Star Wars)'
    },

    'do_re_mi': {
        'sequence': '1 2 3 1| 3 1| 3',
        'delay': 0.4,
        'description': 'Do-Re-Mi (Sound of Music)'
    }
}


def main():
    """Главная функция"""
    print("=" * 60)
    print("🎹 ПРОИГРЫВАТЕЛЬ МЕЛОДИЙ ДЛЯ STM32")
    print("=" * 60)

    # Настройка порта
    port = input("\nВведите COM-порт (или Enter для COM3): ").strip()
    if not port:
        port = 'COM3'

    player = MelodyPlayer(port=port)

    if not player.connect():
        sys.exit(1)

    try:
        while True:
            # Показать список мелодий
            print("\n" + "=" * 60)
            print("📜 ДОСТУПНЫЕ МЕЛОДИИ:")
            print("=" * 60)

            for i, (key, data) in enumerate(MELODIES.items(), 1):
                print(f"{i:2}. {key:20} - {data['description']}")

            print("\n 0. Выход")
            print("=" * 60)

            # Выбор мелодии
            choice = input("\nВыберите номер мелодии: ").strip()

            if choice == '0':
                break

            if choice.isdigit():
                idx = int(choice) - 1
                melody_keys = list(MELODIES.keys())

                if 0 <= idx < len(melody_keys):
                    key = melody_keys[idx]
                    melody = MELODIES[key]
                    player.play_melody(key, melody)
                else:
                    print("❌ Неверный номер!")
            else:
                print("❌ Введите число!")

            # Пауза перед следующим выбором
            input("\nНажмите Enter для продолжения...")

    except KeyboardInterrupt:
        print("\n\n⚠️  Прервано пользователем")
    finally:
        player.disconnect()

    print("\n👋 До свидания!\n")


if __name__ == "__main__":
    main()
