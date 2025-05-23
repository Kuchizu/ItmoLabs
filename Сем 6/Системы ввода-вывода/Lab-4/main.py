import serial
import serial.tools.list_ports

def calculate_crc8(data):
    crc = 0
    for byte in data:
        crc ^= byte
        for _ in range(8):
            if crc & 0x80:
                crc = (crc << 1) ^ 0x07
            else:
                crc = crc << 1
        crc &= 0xFF
    return crc

def write_packet(ser, data):
    # $ | L | data | CRC8
    # Prepare the packet
    sync_byte = bytes([0x5A])
    length_byte = bytes([len(data)])
    crc = calculate_crc8(data)
    crc_byte = bytes([crc])

    # Assemble the full packet
    packet = sync_byte + length_byte + data + crc_byte
    print(packet, data)

    # Send the packet
    bytes_written = ser.write(packet)
    return bytes_written

def read_packet(ser):
    # Read first byte
    sync_byte = ser.read(1)
    if not sync_byte:
        raise ValueError("No data received")

    # Если это не 0x5A, то печатаем как пользовательское сообщение (простой текст)
    if sync_byte[0] != 0x5A:
        leftover = ser.read(ser.in_waiting or 1)
        full_message = sync_byte + leftover
        try:
            print(f"Custom message: {full_message.decode('utf-8', errors='replace')}")
        except UnicodeDecodeError:
            print(f"Custom message (raw): {full_message.hex()}")
        raise ValueError(f"Invalid synchrobyte: {sync_byte.hex()}, expected: 0x5A")

    # Стандартная структура пакета: $ | L | data | CRC
    length_byte = ser.read(1)
    if not length_byte:
        raise ValueError("Failed to read length byte")

    data_length = length_byte[0]
    data = ser.read(data_length)
    if len(data) != data_length:
        raise ValueError(f"Incomplete data: read {len(data)} bytes, expected {data_length} bytes")

    crc_byte = ser.read(1)
    if not crc_byte:
        raise ValueError("Failed to read CRC byte")

    calculated_crc = calculate_crc8(data)

    if calculated_crc != crc_byte[0]:
        raise ValueError(f"CRC check failed: received {crc_byte[0]}, calculated {calculated_crc}")

    return data


def read_single_packet(ser):
    try:
        data = read_packet(ser)
        print(f"Received data (hex): {data.hex()}")
        try:
            print(f"Received data (string): {data.decode('utf-8')}")
        except UnicodeDecodeError:
            print("Received data is not a valid UTF-8 string")
        return True
    except ValueError as ve:
        print(f"Packet error: {ve}")
        return False

def read_continuous(ser):
    print("Waiting for data. Press Ctrl+C to exit...")
    try:
        while True:
            read_single_packet(ser)
    except KeyboardInterrupt:
        print("\nReceiving stopped by user")

def write_message(ser):
    message = input("Enter message to send: ")
    if not message:
        print("Empty message, not sending")
        return
    bytes_written = write_packet(ser, message.encode('utf-8'))
    print(f"Sent {bytes_written} bytes, encoded as {message.encode('utf-8')}")

def display_menu():
    print("\n--- Serial Communication Menu ---")
    print("1. Write a message")
    print("2. Read a single packet")
    print("3. Read continuously")
    print("4. Exit")
    return input("Select option (1-4): ")

def main():
    baudrate = 38400
    ser = None

    try:
        # Find and open the COM port
        ports = serial.tools.list_ports.comports()
        port = next((p.device for p in ports), None)
        if port is None:
            raise ValueError("No COM port found.")

        # ser = serial.Serial(port, baudrate=baudrate)
        ser = serial.Serial(
            port=port,
            baudrate=baudrate,
            bytesize=serial.EIGHTBITS,
            parity=serial.PARITY_ODD,
            stopbits=serial.STOPBITS_TWO,
            timeout=1
        )
        print(f"Connected to {port} at {baudrate} baud")

        while True:
            choice = display_menu()

            if choice == '1':
                write_message(ser)
                read_continuous(ser)
            elif choice == '2':
                print("Reading a single packet...")
                read_single_packet(ser)
            elif choice == '3':
                read_continuous(ser)
            elif choice == '4':
                print("Exiting...")
                break
            else:
                print("Invalid option, please try again.")

    except ValueError as ve:
        print("Error:", str(ve))
    except serial.SerialException as se:
        print("Serial port error:", str(se))
    except Exception as e:
        print("An error occurred:", str(e))
    finally:
        # Ensure serial port is closed
        if ser and ser.is_open:
            ser.close()
            print("Serial port closed")

if __name__ == "__main__":
    main()
