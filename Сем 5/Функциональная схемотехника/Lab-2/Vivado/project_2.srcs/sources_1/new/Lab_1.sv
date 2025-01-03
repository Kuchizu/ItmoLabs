module safe_descending_stack_tb;

    logic clk;                   // �������� ������
    logic rst;                   // �����
    logic write;                 // ������ ������
    logic read;                  // ������ ������
    logic [15:0] data_in;        // ������� ������
    logic [15:0] data_out;       // �������� ������
    logic empty;                 // ���� ������ �������
    logic full;                  // ���� ������ �������

    // ��������� ������������ ������
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

    // ��������� ��������� ������� 100 ���
    initial begin
        clk = 0;
        forever #5 clk = ~clk; // ������ 10 �� (100 ���)
    end

    // �������� ����
    initial begin
        // �����
        rst = 1; write = 0; read = 0; data_in = 0;
        #15 rst = 0;

        // ��������������� ������ ������
        write = 1; data_in = 16'hAAAA; #10;
        write = 1; data_in = 16'hBBBB; #10;
        write = 0; #10; // ���������� ������

        // ������������� `write` � `read`
        @(posedge clk);
        write = 1; read = 1; data_in = 16'hCCCC;

        // �������� ���������
        @(posedge clk);
        if (data_out !== 16'hAAAA)
            $display("Error: Expected 0xAAAA on data_out, got %h", data_out);
        else
            $display("Success: Simultaneous read/write handled correctly, read = 0xAAAA, write = 0xCCCC.");

        // ��������� ����: �������� ������ � ����� ����������
        @(posedge clk);
        write = 1; read = 1; data_in = 16'hDDDD;

        @(posedge clk);
        if (data_out !== 16'hBBBB)
            $display("Error: Expected 0xBBBB on data_out, got %h", data_out);
        else
            $display("Success: Simultaneous read/write handled correctly, read = 0xBBBB, write = 0xDDDD.");

        // ���������� �����
        @(posedge clk);
        write = 0; read = 0;
        $display("Test completed for simultaneous read and write.");
        $finish;
    end
endmodule
