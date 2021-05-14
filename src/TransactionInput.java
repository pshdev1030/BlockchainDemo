public class TransactionInput {
    public String transactionOutputId; // TransactionOutput에서 트랜잭션을 찾는 용도로 사용
    public TransactionOutput UTXO; // 지갑의 잔고 역할

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}