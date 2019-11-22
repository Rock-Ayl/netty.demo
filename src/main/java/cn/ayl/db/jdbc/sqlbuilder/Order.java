package cn.ayl.db.jdbc.sqlbuilder;

public class Order extends Sentence {

    public Order(Sql sql) {
        super(sql, "order serviceBy");
    }

    public Order append(String token) {
        return (Order) super.append(token);
    }

}
