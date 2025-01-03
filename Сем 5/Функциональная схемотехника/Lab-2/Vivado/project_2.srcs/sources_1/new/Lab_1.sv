module safe_descending_stack_tb;

    logic clk;                   // Тактовый сигнал
    logic rst;                   // Сброс
    logic write;                 // Сигнал записи
    logic read;                  // Сигнал чтения
    logic [15:0] data_in;        // Входные данные
    logic [15:0] data_out;       // Выходные данные
    logic empty;                 // Флаг пустой очереди
    logic full;                  // Флаг полной очереди

    // Экземпляр тестируемого модуля
    safe_fifo uut (
        .clk(clk),
        .rst(rst),
        .write(write),
        .read(read),
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

    // Основной тест
    initial begin
        // Сброс
        rst = 1; write = 0; read = 0; data_in = 0;
        #15 rst = 0;

        // Предварительная запись данных
        write = 1; data_in = 16'hAAAA; #10;
        write = 1; data_in = 16'hBBBB; #10;
        write = 0; #10; // Прекращаем запись

        // Одновременные `write` и `read`
        @(posedge clk);
        write = 1; read = 1; data_in = 16'hCCCC;

        // Проверка поведения
        @(posedge clk);
        if (data_out !== 16'hAAAA)
            $display("Error: Expected 0xAAAA on data_out, got %h", data_out);
        else
            $display("Success: Simultaneous read/write handled correctly, read = 0xAAAA, write = 0xCCCC.");

        // Следующий такт: проверка работы с новым состоянием
        @(posedge clk);
        write = 1; read = 1; data_in = 16'hDDDD;

        @(posedge clk);
        if (data_out !== 16'hBBBB)
            $display("Error: Expected 0xBBBB on data_out, got %h", data_out);
        else
            $display("Success: Simultaneous read/write handled correctly, read = 0xBBBB, write = 0xDDDD.");

        // Завершение теста
        @(posedge clk);
        write = 0; read = 0;
        $display("Test completed for simultaneous read and write.");
        $finish;
    end
endmodule
