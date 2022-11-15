try:
    l = list(map(int, input().split()))
    r1, r2, i1, r3, i2, i3, i4 = l

    R1 = (r1 + i1 + i2 + i4) % 2
    R2 = (r2 + i1 + i3 + i4) % 2
    R3 = (r3 + i2 + i3 + i4) % 2

    ll = [R1, R2] + [R3 if i == 3 else l[i] for i in range(2, len(l))]

    print("\nПравильный:")
    print("1  | 2  | 3  | 4  | 5  | 6  | 7  |")
    print("----------------------------------")
    print("r1 | r2 | i1 | r3 | i2 | i3 | i4 |")
    print("----------------------------------")
    print(*ll, sep='  | ')

    R1er = (i1 + i2 + i4) % 2
    R2er = (i1 + i3 + i4) % 2
    R3er = (i2 + i3 + i4) % 2

    er = (R1er != r1) + (R2er != r2) * 2 + (R3er != r3) * 4

    print("\n\nС ошибкой:")
    print("1  | 2  | 3  | 4  | 5  | 6  | 7  |")
    print("----------------------------------")
    print("r1 | r2 | i1 | r3 | i2 | i3 | i4 |")
    print("----------------------------------")

    for i in range(len(l)):
        if i == er - 1:
            print(f"er={l[i]}", end=" | ")
        else:
            print(l[i], end="  | ")

except Exception as e:
    print('Invalid input')
