package projects.chirolhill.juliette.carpediem.model;

public class DatabaseMoment implements DatabaseAdapter {
    public String title;
    public String date;
    public String imgUrl;

    public DatabaseMoment() { }

    public DatabaseMoment(Moment m) {
        this.title = m.getTitle();
        this.date = m.getDate();
    }

    @Override
    public Object revertToOriginal() {
        Moment m = new Moment(title, date);
        return m;
//        Customer c = new Customer();
//        c.setuID(this.uID);
//        c.setUsername(this.username);
//        c.setEmail(this.email);
//        c.setMerchant(this.isMerchant);
//
//        // return all orders
//        if(orders != null) {
//            for(String orderID : orders) {
//                c.getLog().addOrder(new Order(orderID));
//            }
//        }
//
//        return c;
    }
}
