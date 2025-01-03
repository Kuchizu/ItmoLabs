`timescale 1ns/1ps
module majority_5input_nor_tb;
reg a_in, b_in, c_in, d_in, e_in;
wire y_out;

majority_5input_nor majority_5input_nor_1(
    .A(a_in),
    .B(b_in),
    .C(c_in),
    .D(d_in),
    .E(e_in),
    .Y(y_out)
);
    integer i;
    integer j;
    integer sum;
    reg [4:0] test_val; 
    reg expected_val;
    initial begin
    for (i=0; i<32; i=i+1) begin 
        sum = 0;
        test_val = i; 
        expected_val=0;
        a_in = test_val[0];
        b_in = test_val[1];
        c_in = test_val[2];
        d_in = test_val[3];
        e_in = test_val[4];
        for (j=0; j<5; j=j+1) begin
            sum = sum + test_val[j];
        end

        if (sum >= 3) begin
            expected_val=1;
        end

        #10

        if (y_out == expected_val) begin
        $display("The adder output is correct!!! a_in=%b,  b_in=%b, c_in=%b, d_in=%b, e_in=%b y_out = %b", a_in, b_in, c_in, d_in, e_in, y_out);
        end else begin
        $display("The adder output is wrong!!! a_in=%b,  b_in=%b, c_in=%b, d_in=%b, e_in=%b, y_out = %b, expected = %b", a_in, b_in, c_in, d_in, e_in, y_out, expected_val);
    end
end
    #10 $stop;
end
endmodule