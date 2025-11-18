#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Тест звука - простая отправка команд
"""

import serial
import time
import sys

def test_sound(port='COM7'):
    """Тест воспроизведения звука"""
    print(f"Подключение к {port}...")

    try:
        ser = serial.Serial(port, 115200, timeout=1)
        time.sleep(2)
        print("Подключено!\n")

        # Читаем приветственное сообщение
        time.sleep(1)
        while ser.in_waiting > 0:
            data = ser.readline()
            try:
                print(data.decode('utf-8', errors='ignore').strip())
            except:
                pass

        print("\n" + "="*50)
        print("ТЕСТ ЗВУКА")
        print("="*50)

        # Тест 1: Одиночная нота
        print("\n1. Тест одиночной ноты '1' (До)...")
        ser.write(b'1')
        time.sleep(1.5)

        # Тест 2: Последовательность нот
        print("\n2. Тест гаммы '1234567'...")
        for note in '1234567':
            ser.write(note.encode())
            time.sleep(0.4)

        # Тест 3: Смена октавы
        print("\n3. Тест смены октавы '+'...")
        ser.write(b'+')
        time.sleep(0.5)
        ser.write(b'1')
        time.sleep(1.5)

        # Тест 4: Длительность
        print("\n4. Тест изменения длительности 'AAA'...")
        ser.write(b'AAA')
        time.sleep(0.5)
        ser.write(b'1')
        time.sleep(4)

        # Тест 5: Вся гамма через Enter
        print("\n5. Тест воспроизведения всей гаммы (Enter)...")
        ser.write(b'\r')
        time.sleep(10)

        # Читаем ответы
        print("\n" + "="*50)
        print("ОТВЕТЫ ОТ STM32:")
        print("="*50)
        time.sleep(1)
        while ser.in_waiting > 0:
            data = ser.readline()
            try:
                msg = data.decode('utf-8', errors='ignore').strip()
                if msg:
                    print(f"STM32: {msg}")
            except:
                pass

        print("\n✅ Тест завершен!")

    except serial.SerialException as e:
        print(f"❌ Ошибка: {e}")
        print("\nПроверьте:")
        print("  1. Правильно ли указан COM-порт")
        print("  2. Подключен ли микроконтроллер")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n⚠️  Прервано")
    finally:
        if 'ser' in locals() and ser.is_open:
            ser.close()
            print("Отключено")


if __name__ == "__main__":
    if len(sys.argv) > 1:
        port = sys.argv[1]
    else:
        port = input("Введите COM-порт (или Enter для COM7): ").strip()
        if not port:
            port = 'COM7'

    test_sound(port)
