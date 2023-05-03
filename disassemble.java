import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


public class disassemble {
	public static void main(String[] args) throws IOException {
		Path path = Paths.get(args[0]);
		byte[] fileContents =  Files.readAllBytes(path);
		/*
        int bit = fileContents[0];
        File file = new File("decoded.legv8asm");
        try {
             file.createNewFile();
             FileWriter writer = new FileWriter(file);
             writer.write(decoder(fileContents));
             writer.close();
        } catch (IOException e) {
        	System.out.println("");
         }
         */
        System.out.println(decoder(fileContents));
	}

    static class rType{
        int rm;
        int shamt;
        int rn;
        int rd;
        public rType(int rm, int shamt, int rn, int rd){
            this.rm = rm;
            this.shamt = shamt;
            this.rn = rn;
            this.rd = rd;
        }
    }
    static class iType{
        int imm;
        int rn;
        int rd;
        public iType(int imm, int rn, int rd){
            this.imm = imm;
            this.rn = rn;
            this.rd = rd;
        }
    }
    static class dType{
        int dt_address;
        int op;
        int rn;
        int rt;
        public dType(int dt_address, int op, int rn, int rt){
            this.dt_address = dt_address;
            this.op = op;
            this.rn = rn;
            this.rt = rt;
        }
    }
    static class bType{
        int branch_address;
        public bType(int branch_address){
            this.branch_address = branch_address;
        }
    }
    static class cbType{
        int branch_address;
        int rt;
        public cbType(int branch_address, int rt){
            this.branch_address = branch_address;
            this.rt = rt;
        }
    }
	private static String getBinaryString(byte b) {
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}
    private static rType setRType(byte[] byteArray, int start){
        int Rm = 0;
        int Shamt = 0;
        int Rn = 0;
        int Rd = 0;
        int temp1 = 0;
        int temp2 = 0;

        temp1 = Byte.toUnsignedInt( byteArray[start + 1]);//byte2
        temp1  = (byte) (temp1 & ((1 << 5) - 1));
        Rm  =  temp1;

        temp1 = Byte.toUnsignedInt(byteArray[start + 2]);//byte3
        temp1 = temp1 >> 2;
        Shamt = temp1;

        temp1 = Byte.toUnsignedInt(byteArray[start + 2]); //byte 3
        temp2 = Byte.toUnsignedInt(byteArray[start + 3]); //byte 4
        temp1  = (byte) (temp1 & ((1 << 2) - 1));
        temp1 = temp1 << 3;
        temp2 = temp2 >> 5;
        Rn = temp1  + temp2;

        temp1 = Byte.toUnsignedInt(byteArray[start + 3]);//byte 4
        temp1  = (byte) (temp1 & ((1 << 5) - 1));
        Rd = temp1;

       return new rType(Rm,Shamt,Rn,Rd);
    }
    private static iType setIType(byte[] byteArray, int start){
        int ALU_immediate = 0;
        int Rn = 0;
        int Rt = 0;
        int temp1 = 0;
        int temp2 = 0;

        temp1 = Byte.toUnsignedInt(byteArray[start + 1]); //byte 2
        temp2 = Byte.toUnsignedInt(byteArray[start + 2]); //byte 3
        temp1 = temp1 << 2;
        temp2 = temp2 >> 2;
        temp1 *= Math.pow(2, 4);
        ALU_immediate = temp1 + temp2;

        temp1 = Byte.toUnsignedInt(byteArray[start + 2]); //byte 3
        temp2 = Byte.toUnsignedInt(byteArray[start + 3]); //byte 4
        temp1 = temp1 << 3;
        temp1  = (byte) (temp1 & ((1 << 5) - 1));
        temp2 = temp2 >> 5;
        Rn = temp1 + temp2;

        temp1 = 0;
        temp1 = Byte.toUnsignedInt(byteArray[start + 3]);//byte 4
        //temp1 = temp1 << 3;
        //temp1 = temp1 >> 3;
        temp1  = (byte) (temp1 & ((1 << 5) - 1));
        Rt = temp1;

        return new iType(ALU_immediate, Rn, Rt);
    }
    private static dType setDType(byte[] byteArray, int start){
        int DT_address = 0;
        int op = 0;
        int Rn = 0;
        int Rd = 0;
        int temp1 = 0;
        int temp2 = 0;

        temp1 = Byte.toUnsignedInt(byteArray[start + 1]); //byte 2
        temp2 = Byte.toUnsignedInt(byteArray[start + 2]);//byte 3
        temp1  = (byte) (temp1 & ((1 << 5) - 1));
        temp1 *= 2;
        temp2 = temp2 >> 4;
        DT_address = temp1 + temp2;

        temp1 = Byte.toUnsignedInt(byteArray[start + 2]);//byte 3
        temp1 = temp1 >> 2;
        temp1  = (byte) (temp1 & ((1 << 2) - 1));
        op = temp1;

        temp1 = Byte.toUnsignedInt(byteArray[start + 2]);//byte 3
        temp2 = Byte.toUnsignedInt(byteArray[start + 3]);//byte 4
        temp1  = (byte) (temp1 & ((1 << 2) - 1));
        temp1 = temp1 << 3;
        temp2 = temp2 >> 5;
        Rn = temp1 + temp2;

        temp1 = Byte.toUnsignedInt(byteArray[start + 3]);//byte 4
        temp1  = (byte) (temp1 & ((1 << 5) - 1));
        Rd = temp1;

        return new dType(DT_address, op, Rn, Rd);
    }
    private static bType setBType(byte[] byteArray, int start){
        int BR_address = 0;
        int temp1 = 0;
        int temp2 = 0;
        int temp3 = 0;
        int temp4 = 0;

        temp1 = byteArray[start];						 //byte 1 00010111
        temp2 = byteArray[start + 1];//byte 2 11111111
        temp3 = byteArray[start + 2];//byte 3 11111111
        temp4 = byteArray[start + 3];//byte 4 11100110
        temp1  = (byte) (temp1 & ((1 << 2) - 1));

        if(temp1 == 3 || temp1 == 2) {
	        BR_address = BR_address << 2;
	        BR_address += temp1; //111...(11)temp1
	        BR_address = BR_address << 8; //room for temp2
	        BR_address = BR_address | temp2;
	        BR_address = BR_address << 8;
	        BR_address = BR_address | temp3;
	        BR_address = BR_address << 8;
	        BR_address = BR_address | temp4; //11111111
	        								 //11100110
        }else {
        	temp2 = Byte.toUnsignedInt(byteArray[start + 1]);//byte 2
            temp3 = Byte.toUnsignedInt(byteArray[start + 2]);//byte 3
            temp4 = Byte.toUnsignedInt(byteArray[start + 3]);//byte 4
            temp1 = temp1 << 24;
            temp2 = temp2 << 16;
            temp3 = temp3 << 8;
            BR_address = temp1 + temp2 + temp3 + temp4;
        }
        return new bType(BR_address);
    }
    private static cbType setCBType(byte[] byteArray, int start){
        int COND_BR_address = 0;
        int Rt = 0;
        int temp1 = 0;
        int temp2 = 0;
        int temp3 = 0;

        temp1 = byteArray[start + 1];//byte 2
        temp2 = Byte.toUnsignedInt(byteArray[start + 2]);//byte 3
        temp3 = Byte.toUnsignedInt(byteArray[start + 3]);//byte 4
        temp1 = temp1 << 11;
        temp2 = temp2 << 3;
        temp3 = temp3 >> 5;
        COND_BR_address = temp1 + temp2 + temp3;


        temp1 = Byte.toUnsignedInt(byteArray[start + 3]);//byte 4
        temp1  = (byte) (temp1 & ((1 << 5) - 1));
        Rt = temp1;
        return new cbType(COND_BR_address, Rt);
    }
    private static String decoder(byte[] byteArray){

        String instruction = "";
        //We would have to figure this out each iteration
        //Default is -1, -1 should never actually be seen or else it's wrong
        int opCode = -1;
        int rm = -1;
        int rd = -1;
        int rn = -1;
        int rt = -1;
        int branch_address = -1;
        int shamt = -1;
        int imm = -1;
        int dt_address = -1;
        int instructionCount = 1;
        String totalInstruction = "";
        //NOTE: Syntax of the way the instruction is written is different from the
        //      format of the way the code is stored. For example, ADD is written
        //      as "ADD Rd, Rn, Rm" (like ADD X1, X2, X3) but the format is opCode, rm, shamt, rn, rd
        for(int i = 0; i < byteArray.length; i = i + 4){
            //Find the opcode for our instruction here
            for(int v = 0; v<4; v++){
                if(v==0){
                    int temp1 = Byte.toUnsignedInt(byteArray[i]);//byte 1
                    temp1 = temp1 >>2;
                    temp1 = temp1<<2;
                    temp1 *= Math.pow(2, -2);
                    opCode = temp1;
                    //System.out.println("6 opcode: " + opCode);
                    //6 opCodes
                    if(opCode == 5){
                        //This is B, not B.cond, that is a different one
                        //B instruction
                        //Format: opCode, BR_address

                        bType inst = setBType(byteArray, i);
                        branch_address = inst.branch_address;
                        instruction = "B " + "line_" +(instructionCount+branch_address);
                        break;
                    }
                    else if(opCode == 37){
                        //signed = -27
                        //BL instruction
                        //Format: opCode, BR_address
                        bType inst = setBType(byteArray, i);
                        branch_address = inst.branch_address;
                        instruction = "BL " + "line_" +(instructionCount+branch_address);
                        break;
                    }
                }
                else if(v==1){
                    int temp1 = Byte.toUnsignedInt(byteArray[i]);//byte 1
                    opCode = temp1;
                   // System.out.println("8 opCode: " + opCode);
                    //8 opCodes
                    if(opCode == 180){
                        //signed = -76
                        //CBZ instruction
                        //Format: opCode, COND_BR_address, Rt
                        cbType inst = setCBType(byteArray, i);
                        rt = inst.rt;
                        branch_address = inst.branch_address;
                        if(branch_address != -1){   //COND_BR_address is nothing, maybe change it to 0?
                            instruction = "CBZ X" + rt + ", " + "line_" +(instructionCount+branch_address);
                        }
                        else{
                            instruction = "CBZ X" + rt;
                        }
                        break;
                    }
                    else if(opCode == 181){
                        //signed = -75
                        //CBNZ instruction
                        //Format: opCode, COND_BR_address, Rt
                        cbType inst = setCBType(byteArray, i);
                        rt = inst.rt;
                        branch_address = inst.branch_address;
                        if(branch_address != -1){   //COND_BR_address is nothing, maybe change it to 0?
                            instruction = "CBNZ X" + rt + ", " + "line_" +(instructionCount+branch_address);
                        }
                        else{
                            instruction = "CBNZ X" + rt;
                        }
                        break;
                    }
                    else if (opCode == 84){
                        //B.cond instruction
                        //Rt is the cond
                        //Format: opCode, COND_BR_address, Rt
                        cbType inst = setCBType(byteArray, i);
                        rt = inst.rt;
                        branch_address = inst.branch_address;
                        if(rt == 0){
                            instruction = "B.EQ ";
                        }
                        else if (rt == 1){
                            instruction = "B.NE ";
                        }
                        else if (rt == 2){
                            instruction = "B.HS ";
                        }
                        else if (rt == 3){
                            instruction = "B.LO ";
                        }
                        else if (rt == 4){
                            instruction = "B.MI ";
                        }
                        else if (rt == 5){
                            instruction = "B.PL ";
                        }
                        else if (rt == 6){
                            instruction = "B.VS ";
                        }
                        else if (rt == 7){
                            instruction = "B.VC ";
                        }
                        else if (rt == 8){
                            instruction = "B.HI ";
                        }
                        else if (rt == 9){
                            instruction = "B.LS ";
                        }
                        else if (rt == 10){
                            instruction = "B.GE ";
                        }
                        else if (rt == 11){
                            instruction = "B.LT ";
                        }
                        else if (rt == 12){
                            instruction = "B.GT ";
                        }
                        else if (rt == 13){
                            instruction = "B.LE ";
                        }

                        if(branch_address != -1){   //COND_BR_address is nothing, maybe change it to 0?
                            instruction += "line_" +  (instructionCount + branch_address);
                        }
                        break;
                    }
                }
                else if(v==2){
                    int temp1 = Byte.toUnsignedInt(byteArray[i]);//byte 1
                    int temp2 = Byte.toUnsignedInt(byteArray[i+1]);//byte 2
                    temp2 = temp2 >>6;
                    temp1 *= Math.pow(2, 2);
                    opCode = temp1 + temp2;
                   // System.out.println("10 opCode: " + opCode);
                    //10 opCodes
                    if(opCode == 580){     //TO-DO: Find rm, rn, and immediate
                        //signed is -444
                        //ADDI instruction
                        //Format: opCode, imm, rn, rd
                        iType inst = setIType(byteArray, i);
                        rd = inst.rd;
                        rn = inst.rn;
                        imm = inst.imm;
                        instruction = "ADDI X" + rd + ", X" + rn + ", #" + imm;
                        break;
                    }
                    else if(opCode == 584){     //TO-DO: Find rd, rn, and immediate
                        //signed = -440
                        //ANDI instruction
                        //Format: opCode, imm, rn, rd
                        iType inst = setIType(byteArray, i);
                        rd = inst.rd;
                        rn = inst.rn;
                        imm = inst.imm;
                        instruction = "ANDI X" + rd + ", X" + rn + ", #" + imm;
                        break;
                    }
                    else if(opCode == 840){     //TO-DO: Find rd, rn, and immediate
                        //signed = -184
                        //EORI instruction
                        //Format: opCode, imm, rn, rd
                        iType inst = setIType(byteArray, i);
                        rd = inst.rd;
                        rn = inst.rn;
                        imm = inst.imm;
                        instruction = "EORI X" + rd + ", X" + rn + ", #" + imm;
                        break;
                    }
                    else if(opCode == 712){     //TO-DO: Find rd, rn, and immediate
                        //siged = -312
                        //ORRI instruction
                        //Format: opCode, imm, rn, rd
                        iType inst = setIType(byteArray, i);
                        rd = inst.rd;
                        rn = inst.rn;
                        imm = inst.imm;
                        instruction = "ORRI X" + rd + ", X" + rn + ", #" + imm;
                        break;
                    }
                    else if(opCode == 836){     //TO-DO: Find rd, rn, and immediate
                        //signed = -188
                        //SUBI instruction
                        //Format: opCode, imm, rn, rd
                        iType inst = setIType(byteArray, i);
                        rd = inst.rd;
                        rn = inst.rn;
                        imm = inst.imm;
                        instruction = "SUBI X" + rd + ", X" + rn + ", #" + imm;
                        break;
                    }
                    else if(opCode == 964){     //TO-DO: Find rd, rn, and immediate and Set flags(?)
                        //signed = -60
                        //SUBIS instruction
                        //Format: opCode, imm, rn, rd
                        iType inst = setIType(byteArray, i);
                        rd = inst.rd;
                        rn = inst.rn;
                        imm = inst.imm;
                        instruction = "SUBIS X" + rd + ", X" + rn + ", #" + imm;
                        break;
                    }
                }
                else if(v==3){
                    int temp1 = Byte.toUnsignedInt(byteArray[i]);//byte 1
                    int temp2 = Byte.toUnsignedInt(byteArray[i+1]);//byte 2
                    temp2 = temp2 >>5;
                    temp1 *= Math.pow(2, 3);
                    opCode = temp1 + temp2;
                   // System.out.println("11 opCode: " + opCode);
                    //11 opCodes
                    if(opCode == 1112){    //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed is -936
                        //ADD instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "ADD " + "X" + rd + ", X" + rn + ", X" + rm;
                        break;
                    }
                    else if(opCode == 1104){    //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed is -944
                        //AND instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "AND "+ "X" + rd + ", X" + rn + ", X" + rm;
                        break;
                    }
                    else if(opCode == 1712){    //TO-DO: Find rd, rm, shamt(?) and rn
                        //signd = -336
                        //BR instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rt = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "BR X" + rn;
                        break;
                    }
                    else if(opCode == 2046){    //TO-DO: The rest of the instruction
                        //signed = -2
                        //DUMP instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "DUMP";
                        break;
                    }
                    else if(opCode == 1616){    //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed -432
                        //EOR instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "EOR "+ "X" + rd + ", X" + rn + ", X" + rm;
                        break;
                    }
                    else if(opCode == 2047){    //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed = -1
                        //HALT instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "HALT";
                        break;
                    }
                    else if(opCode == 1986){     //TO-DO: Find rt, rn, and dt_address
                        //signed = -62
                        //LDUR instruction
                        //Format: opCode, dt_address, rn, rt
                        dType inst = setDType(byteArray, i);
                        rt = inst.rt;
                        rn = inst.rn;
                        dt_address = inst.dt_address;
                        instruction = "LDUR X" + rt + ", [X" + rn + ", #" + dt_address + "]";
                        break;
                    }
                    else if(opCode == 1691){     //TO-DO: Find rd, rm(?), shamt and rn
                        //signed = -357
                        //LSL instruction
                        //Format: opCode, rm, shamt, rn, rd
                        dType inst = setDType(byteArray, i);
                        rt = inst.rt;
                        rn = inst.rn;
                        dt_address = inst.dt_address;
                        instruction = "LSL X" + rd + ", X" + rn + ", #" + shamt;
                        break;
                    }
                    else if(opCode == 1690){     //TO-DO: Find rd, rm(?), shamt and rn
                        //signed = -358
                        //LSR instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "LSR X" + rd + ", X" + rn + ", #" + shamt;
                        break;
                    }
                    else if(opCode == 1360){     //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed -688
                        //ORR instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "ORR "+ "X" + rd + ", X" + rn + ", X" + rm;
                        break;
                    }
                    else if(opCode == 1984){     //TO-DO: Find rt, rn, and dt_address
                        //signed -64
                        //STUR instruction
                        //Format: opCode, dt_address, rn, rt
                        dType inst = setDType(byteArray, i);
                        rt = inst.rt;
                        rn = inst.rn;
                        dt_address = inst.dt_address;
                        instruction = "STUR X"+ rt + ", [X" + rn + ",#" + dt_address + "]";
                        break;
                    }
                    else if(opCode == 1624){     //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed -424
                        //SUB instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "SUB "+ "X" + rd + ", X" + rn + ", X" + rm;
                        break;
                    }
                    else if(opCode == 1880){     //TO-DO: Find rd, rm, shamt(?), rn, and set flags(?)
                        //signed -168
                        //SUBS instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "SUBS "+ "X" + rd + ", X" + rn + ", X" + rm;
                        break;
                    }
                    else if(opCode == 1240){     //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed = -808
                        //MUL instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "MUL X" + rd + ", X" + rn + ", X" + rm;
                        break;
                    }
                    else if(opCode == 2045){     //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed -3
                        //PRNT instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "PRNT" + " X"+rd;
                        break;
                    }
                    else if(opCode == 2044){     //TO-DO: Find rd, rm, shamt(?) and rn
                        //signed -4
                        //PRNL instruction
                        //Format: opCode, rm, shamt, rn, rd
                        rType inst = setRType(byteArray, i);
                        rd = inst.rd;
                        rm = inst.rm;
                        shamt = inst.shamt;
                        rn = inst.rn;
                        instruction = "PRNL";
                        break;
                    }
                }


            }
            //System.out.println("Instruction: " + instruction);
            totalInstruction += "\n" + "line_" + instructionCount +":";
            totalInstruction += "\n" + instruction;
            instruction = "";
            instructionCount++; //I KNOW THIS IS REDUNDANT, I DON'T WANT TO CHANGE IT, not anymore : )
        }
        return totalInstruction;
    }

}

