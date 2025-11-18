#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Простой клиент для музыкальной клавиатуры STM32
"""

import serial
import sys

def main():
    # Настройки подключения
    PORT = 'COM7'  # Измените на ваш порт (COM3, COM4, /dev/ttyUSB0 и т.д.)
    BAUDRATE = 115200

    print("=" * 60)
    print("🎹 Простой клиент музыкальной клавиатуры STM32")
    print("=" * 60)
    print(f"\nПодключение к {PORT} на {BAUDRATE} baud...\n")

    try:
        # Подключение к UART
        ser = serial.Serial(PORT, BAUDRATE, timeout=1)
        print("✅ Подключено!\n")
        print("Команды:")
        print("  1-7: Ноты (До-Си)")
        print("  +/-: Октава вверх/вниз")
        print("  A/a: Длительность +/-")
        print("  Enter: Играть гамму")
        print("  quit: Выход\n")

        while True:
            # Ввод команды
            command = input(">>> ").strip()

            if command.lower() in ['quit', 'exit']:
                break

            # Отправка команды
            for char in command:
                ser.write(char.encode())

            # Если нажат Enter, отправляем \r
            if not command:
                ser.write(b'\r')

            # Чтение ответа (если есть)
            while ser.in_waiting > 0:
                response = ser.readline().decode('utf-8', errors='ignore').strip()
                if response:
                    print(f"STM32: {response}")

    except serial.SerialException as e:
        print(f"❌ Ошибка: {e}")
        print("\nПроверьте:")
        print("  1. Правильно ли указан COM-порт")
        print("  2. Подключен ли микроконтроллер")
        print("  3. Установлены ли драйвера")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n\n⚠️  Прервано")
    finally:
        if 'ser' in locals() and ser.is_open:
            ser.close()
            print("\n🔌 Отключено")

    print("👋 До свидания!\n")


if __name__ == "__main__":
    main()
