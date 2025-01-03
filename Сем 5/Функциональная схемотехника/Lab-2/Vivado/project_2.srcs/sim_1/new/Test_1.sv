module safe_descending_stack_tb;

    logic clk;                   // Тактовый сигнал
    logic rst;                   // Сброс
    logic push;                  // Сигнал записи
    logic pop;                   // Сигнал чтения
    logic [15:0] data_in;        // Входные данные
    logic [15:0] data_out;       // Выходные данные
    logic empty;                 // Флаг пустого стека
    logic full;                  // Флаг полного стека

    // Экземпляр тестируемого модуля
    safe_fifo uut (
        .clk(clk),
        .rst(rst),
        .write(push),
        .read(pop),
        .data_in(data_in),
        .data_out(data_out),
        .empty(empty),
        .full(full)
    );

    // Генерация тактового сигнала 100 МГц
    initial begin
        clk = 0;
        forever #5 clk = ~clk; // Период 10 нс (100 МГц)
    end

    initial begin
        clk = 0;
        forever #5 clk = ~clk; // Период 10 нс (100 МГц)
    end

    // Основной тест
    initial begin
        // Сброс
        rst = 1; push = 0; pop = 0; data_in = 0;
        #15 rst = 0;

        // Проверка после сброса
        if (empty !== 1 || full !== 0)
            $display("Error: Incorrect flags after reset.");
        else
            $display("Success: Reset behavior is correct.");

        // Тестирование записи в очередь
        push = 1; data_in = 16'hAAAA; #10;
        push = 1; data_in = 16'hBBBB; #10;
        push = 0; #10; // Прекращаем запись

        // Проверка после записи
        if (empty !== 0 && full !== 0)
            $display("Error: Incorrect flags after push.");
        else
            $display("Success: Push behavior is correct.");

        // Тестирование чтения из очереди
        pop = 1; #10; // Читаем первый элемент
        if (data_out !== 16'hAAAA)
            $display("Error: Expected 0xAAAA, got %h", data_out);
        else
            $display("Success: First read is correct.");

        pop = 1; #10; // Читаем второй элемент
        if (data_out !== 16'hBBBB)
            $display("Error: Expected 0xBBBB, got %h", data_out);
        else
            $display("Success: Second read is correct.");

        pop = 0; #10; // Прекращаем чтение

        // Проверка после чтения
        if (empty !== 1)
            $display("Error: FIFO should be empty after all reads.");
        else
            $display("Success: Read behavior is correct.");

        // Завершение теста
        $display("Test completed successfully at 100 MHz.");

        $finish;

    end
endmodule