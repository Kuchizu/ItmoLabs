public class Program {
    public static void main(String[] args) {
        int x = 100;
        if (x > 0)
            if (x < 50) {
                System.out.println("Greater than zero but less than 50");
            }
           else {
              System.out.println("Less than or equal to zero");
            }
    }
}

Program
|
|-- main
    |
    |-- Declaration (int x = 100)
    |
    |-- If
        |
        |-- Condition (x > 0)
        |
        |-- Then
        |   |
        |   |-- If
        |       |
        |       |-- Condition (x < 50)
        |       |
        |       |-- Then
        |       |   |
        |       |   |-- Print ("Greater than zero but less than 50")
        |       |
        |       |-- Else
        |           |
        |           |-- Print ("Greater than or equal to 50")
        |
        |-- Else
            |
            |-- Print ("Less than or equal to zero")
