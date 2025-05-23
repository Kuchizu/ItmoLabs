import serial
import struct
import time
import argparse

# Константы
SYNC_BYTE = 0x5A


def calculate_crc8(data):
    """Расчет CRC8 для массива байт"""
    crc = 0
    for byte in data:
        crc ^= byte
        for _ in range(8):
            if crc & 0x80:
                crc = ((crc << 1) ^ 0x07) & 0xFF
            else:
                crc = (crc << 1) & 0xFF
    return crc


def send_packet(ser, data):
    """Отправка корректного пакета"""
    # Формируем пакет
    packet = bytearray([SYNC_BYTE, len(data)])
    packet.extend(data)

    # Добавляем CRC
    crc = calculate_crc8(packet)
    packet.append(crc)

    # Отправляем
    ser.write(packet)
    print(f"Отправлен пакет: {' '.join(f'0x{b:02X}' for b in packet)}")

    # Ожидаем ответ
    time.sleep(0.1)
    if ser.in_waiting:
        response = ser.read(ser.in_waiting)
        print(f"Получен ответ: {' '.join(f'0x{b:02X}' for b in response)}")
        parse_packet(response)
    else:
        print("Нет ответа")


def send_invalid_packet(ser, error_type):
    """Отправка некорректного пакета для тестирования обработки ошибок"""
    if error_type == "no_sync":
        # Пакет без синхробайта
        packet = bytearray([0x00, 0x04, 0x01, 0x02, 0x03, 0x04])
        crc = calculate_crc8(packet)
        packet.append(crc)
    elif error_type == "wrong_length":
        # Пакет с некорректной длиной
        packet = bytearray([SYNC_BYTE, 0xFF, 0x01, 0x02])
        crc = calculate_crc8(packet)
        packet.append(crc)
    elif error_type == "wrong_crc":
        # Пакет с неправильной CRC
        packet = bytearray([SYNC_BYTE, 0x04, 0x01, 0x02, 0x03, 0x04, 0x00])

    ser.write(packet)
    print(
        f"Отправлен некорректный пакет ({error_type}): {' '.join(f'0x{b:02X}' for b in packet)}"
    )

    # Ожидаем ответ (хотя его не должно быть для некорректного пакета)
    time.sleep(0.1)
    if ser.in_waiting:
        response = ser.read(ser.in_waiting)
        print(f"Получен ответ: {' '.join(f'0x{b:02X}' for b in response)}")
        parse_packet(response)
    else:
        print("Нет ответа (ожидаемо для некорректного пакета)")


def parse_packet(data):
    """Разбор полученного пакета"""
    if not data or len(data) < 3:
        print("Пакет слишком короткий или отсутствует")
        return

    # Ищем синхробайт
    if data[0] != SYNC_BYTE:
        print("Ошибка: не найден синхробайт")
        return

    # Получаем длину данных
    length = data[1]

    # Проверяем, что пакет имеет достаточную длину
    if len(data) < length + 3:
        print("Ошибка: недостаточно данных в пакете")
        return

    # Получаем данные
    payload = data[2 : 2 + length]

    # Получаем CRC
    received_crc = data[2 + length]

    # Проверяем CRC
    calculated_crc = calculate_crc8(data[: 2 + length])

    if calculated_crc != received_crc:
        print(
            f"Ошибка CRC: получено 0x{received_crc:02X}, рассчитано 0x{calculated_crc:02X}"
        )
        return

    # Если данные - это показания датчика (8 байт для двух float значений)
    if length == 8:
        temperature = struct.unpack("f", bytes(payload[:4]))[0]
        pressure = struct.unpack("f", bytes(payload[4:]))[0]
        print(f"Температура: {temperature:.2f}°C, Давление: {pressure:.2f} Па")
    else:
        print(f"Данные: {' '.join(f'0x{b:02X}' for b in payload)}")


def monitor_sensor_data(ser):
    """Мониторинг данных с датчика"""
    print("Ожидание данных с датчика (нажмите Ctrl+C для выхода)...")
    try:
        while True:
            if ser.in_waiting:
                data = ser.read(ser.in_waiting)
                # Ищем начало пакета (синхробайт)
                start_idx = -1
                for i in range(len(data)):
                    if data[i] == SYNC_BYTE:
                        start_idx = i
                        break

                if start_idx >= 0:
                    parse_packet(data[start_idx:])
            time.sleep(0.01)
    except KeyboardInterrupt:
        print("Мониторинг остановлен")


def main():
    parser = argparse.ArgumentParser(
        description="UART клиент для лабораторной работы №4"
    )
    parser.add_argument(
        "--port", type=str, default="COM7", help="Последовательный порт"
    )
    args = parser.parse_args()

    # Открываем порт с правильными настройками для варианта 1
    try:
        ser = serial.Serial(
            port=args.port,
            baudrate=19200,
            parity=serial.PARITY_EVEN,
            stopbits=serial.STOPBITS_ONE,
            bytesize=serial.EIGHTBITS,
            timeout=1,
        )
        print(f"Порт {args.port} открыт")

        while True:
            print("\nМеню:")
            print("1 - Отправить корректный пакет")
            print("2 - Отправить пакет без синхробайта")
            print("3 - Отправить пакет с некорректной длиной")
            print("4 - Отправить пакет с некорректной CRC")
            print("5 - Мониторинг данных с датчика")
            print("0 - Выход")

            choice = input("Выберите действие: ")

            if choice == "1":
                # Отправляем тестовый пакет с 4 байтами данных
                data = bytearray([0x01, 0x02, 0x03, 0x04])
                send_packet(ser, data)
            elif choice == "2":
                send_invalid_packet(ser, "no_sync")
            elif choice == "3":
                send_invalid_packet(ser, "wrong_length")
            elif choice == "4":
                send_invalid_packet(ser, "wrong_crc")
            elif choice == "5":
                monitor_sensor_data(ser)
            elif choice == "0":
                break
            else:
                print("Неверный выбор")

        ser.close()
        print(f"Порт {args.port} закрыт")

    except serial.SerialException as e:
        print(f"Ошибка при открытии порта {args.port}: {e}")


if __name__ == "__main__":
    main()
