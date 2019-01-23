package org.theta.tx.types;

import java.math.BigInteger;
import java.util.ArrayList;

import org.ethereum.util.RLP;
import org.spongycastle.util.Arrays;

public final class SendTx implements Tx {

    public Coins      fee;
    public TxInput[]  inputs;
    public TxOutput[] outputs;

    public SendTx(byte[] senderAddr, byte[] receiverAddr, BigInteger thetaWei, BigInteger gammaWei, 
        BigInteger feeInGammaWei, long senderSequence) {
        this.fee = new Coins(BigInteger.valueOf(0), feeInGammaWei);

        TxInput txInput = new TxInput(senderAddr, thetaWei, gammaWei.add(feeInGammaWei), senderSequence);
        this.inputs = new TxInput[]{txInput};

        TxOutput txOutput = new TxOutput(receiverAddr, thetaWei, gammaWei);
        this.outputs = new TxOutput[]{txOutput};
    }

    public void setSignature(byte[] signature) throws Exception {
        int numInputs = this.inputs.length;
        if (numInputs != 1) {
            throw new Exception("This demo only supports one sender!");
        }
        this.inputs[0].setSignature(signature);
    }

    public byte[] signBytes(String chainID) throws Exception {
        int numInputs = this.inputs.length;
        if (numInputs != 1) {
            throw new Exception("This demo only supports one sender!");
        }

        // Detach the signatures from the input if existed
        byte[] originalSignature = this.inputs[0].signature;
        this.inputs[0].signature = new byte[0];

        // Serialize the transaction
        byte[] payload = new byte[0];
        payload = Arrays.concatenate(payload, RLP.encode(chainID));
        payload = Arrays.concatenate(payload, RLP.encode(Constant.TxTypeSend));
        payload = Arrays.concatenate(payload, this.rlpEncode());

        EthereumTxWrapper ethTxWrapper = new EthereumTxWrapper(payload);
        byte[] signBytes = ethTxWrapper.rlpEncode();

        // Attach the original signature back to the inputs
        this.inputs[0].signature = originalSignature;

        return signBytes;
    }

    public long getType() {
        return Constant.TxTypeSend;
    }

    public byte[] rlpEncode() {
        int numInputs = this.inputs.length;
        byte[][] inputBytesArray = new byte[numInputs][];
        for (int i = 0; i < numInputs; i ++) {
            inputBytesArray[i] = this.inputs[i].rlpEncode();
        }

        int numOutputs = this.outputs.length;
        byte[][] outputBytesArray = new byte[numOutputs][];
        for (int i = 0; i < numInputs; i ++) {
            outputBytesArray[i] = this.outputs[i].rlpEncode();
        }

        byte[] rlpEncoded = RLP.encodeList(
            this.fee.rlpEncode(),
            RLP.encodeList(inputBytesArray),
            RLP.encodeList(outputBytesArray));
        return rlpEncoded;
    }
}
