`timescale 1ps/1fs

// ----------------------------
// testing $urandom_range with one parameter
//
// author: Tramy Nguyen
// ----------------------------
module system_func1();

    reg bit0;

    initial begin
        bit0 = 1'b0;
    end

    always begin
        #($urandom_range(5)) bit0 = 1'b1;
        #3 bit0 = 1'b0;
    end

endmodule
