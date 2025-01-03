`timescale 1ns / 1ps


module majority_5input_nor(
    input A,
    input B,
    input C,
    input D,
    input E,
    output Y
);

//(A NOR A) NOR (B NOR B) = X 
//(X NOR X) NOR (C NOR C)

// Делаем тройную конъюнкцию
    nor(nA, A, A); 
    nor(nB, B, B); 
    nor(nC, C, C); 
    nor(nD, D, D); 
    nor(nE, E, E);


    nor(AB, nA, nB);
    nor(AC, nA, nC);
    nor(AD, nA, nD);
    nor(AE, nA, nE);
    nor(BC, nB, nC);
    nor(BD, nB, nD);
    nor(BE, nB, nE);
    nor(CD, nC, nD);
    nor(CE, nC, nE);
    nor(DE, nD, nE);
    
    nor(nAB, AB, AB);
    nor(nAC, AC, AC);
    nor(nAD, AD, AD);
    nor(nAE, AE, AE);
    nor(nBC, BC, BC);
    nor(nBD, BD, BD);
    nor(nBE, BE, BE);
    nor(nCD, CD, CD);
    nor(nCE, CE, CE);
    nor(nDE, DE, DE);

    
    nor(ABC, nAB, nC);
    nor(ABD, nAB, nD);
    nor(ABE, nAB, nE);
    nor(ACD, nAC, nD);
    nor(ACE, nAC, nE);
    nor(ADE, nAD, nE);
    nor(BCD, nBC, nD);
    nor(BCE, nBC, nE);
    nor(BDE, nBD, nE);
    nor(CDE, nCD, nE);
 
    // Тройная конъюнкция окончена, начинаем делать дизъюнкции троек

    nor(ABCnorABD, ABC, ABD);
    nor(ABCorABD, ABCnorABD, ABCnorABD);

    nor(ABEnorACD, ABE, ACD);
    nor(ABEorACD, ABEnorACD, ABEnorACD);

    nor(ACEnorADE, ACE, ADE);
    nor(ACEorADE, ACEnorADE, ACEnorADE);

    nor(BCDnorBCE, BCD, BCE);
    nor(BCDorBCE, BCDnorBCE, BCDnorBCE);

    nor(BDEnorCDE, BDE, CDE);
    nor(BDEorCDE, BDEnorCDE, BDEnorCDE);

    nor(ABCorABD_nor_ABEorACD, ABCorABD, ABEorACD);
    nor(ABCorABDorABEorACD, ABCorABD_nor_ABEorACD, ABCorABD_nor_ABEorACD);

    nor(ACEorADE_nor_BCDorBCE, ACEorADE, BCDorBCE);
    nor(ACEorADEorBCDorBCE, ACEorADE_nor_BCDorBCE, ACEorADE_nor_BCDorBCE);    
   
    nor(ABCorABDorABEorACD_nor_ACEorADEorBCDorBCE, ABCorABDorABEorACD, ACEorADEorBCDorBCE);
    nor(ABCorABDorABEorACDorACEorADEorBCDorBCE, ABCorABDorABEorACD_nor_ACEorADEorBCDorBCE, ABCorABDorABEorACD_nor_ACEorADEorBCDorBCE);


    nor(ABCorABDorABEorACDorACEorADEorBCDorBCE_nor_BDEorCDE, ABCorABDorABEorACDorACEorADEorBCDorBCE, BDEorCDE);
    nor(Y, ABCorABDorABEorACDorACEorADEorBCDorBCE_nor_BDEorCDE, ABCorABDorABEorACDorACEorADEorBCDorBCE_nor_BDEorCDE);


endmodule