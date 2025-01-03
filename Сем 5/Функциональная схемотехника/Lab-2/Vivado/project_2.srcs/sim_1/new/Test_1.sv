module safe_descending_stack_tb;

    logic clk;                   // �������� ������
    logic rst;                   // �����
    logic push;                  // ������ ������
    logic pop;                   // ������ ������
    logic [15:0] data_in;        // ������� ������
    logic [15:0] data_out;       // �������� ������
    logic empty;                 // ���� ������� �����
    logic full;                  // ���� ������� �����

    // ��������� ������������ ������
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

    // ��������� ��������� ������� 100 ���
    initial begin
        clk = 0;
        forever #5 clk = ~clk; // ������ 10 �� (100 ���)
    end

    initial begin
        clk = 0;
        forever #5 clk = ~clk; // ������ 10 �� (100 ���)
    end

    // �������� ����
    initial begin
        // �����
        rst = 1; push = 0; pop = 0; data_in = 0;
        #15 rst = 0;

        // �������� ����� ������
        if (empty !== 1 || full !== 0)
            $display("Error: Incorrect flags after reset.");
        else
            $display("Success: Reset behavior is correct.");

        // ������������ ������ � �������
        push = 1; data_in = 16'hAAAA; #10;
        push = 1; data_in = 16'hBBBB; #10;
        push = 0; #10; // ���������� ������

        // �������� ����� ������
        if (empty !== 0 && full !== 0)
            $display("Error: Incorrect flags after push.");
        else
            $display("Success: Push behavior is correct.");

        // ������������ ������ �� �������
        pop = 1; #10; // ������ ������ �������
        if (data_out !== 16'hAAAA)
            $display("Error: Expected 0xAAAA, got %h", data_out);
        else
            $display("Success: First read is correct.");

        pop = 1; #10; // ������ ������ �������
        if (data_out !== 16'hBBBB)
            $display("Error: Expected 0xBBBB, got %h", data_out);
        else
            $display("Success: Second read is correct.");

        pop = 0; #10; // ���������� ������

        // �������� ����� ������
        if (empty !== 1)
            $display("Error: FIFO should be empty after all reads.");
        else
            $display("Success: Read behavior is correct.");

        // ���������� �����
        $display("Test completed successfully at 100 MHz.");

        $finish;

    end
endmodule