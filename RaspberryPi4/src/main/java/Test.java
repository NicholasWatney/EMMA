

public class Test {

    public static void main(String[] args) {
        handler(new A());

    }

    public static void handler(A a) {

    }
}

class A {

    public A() {
        throw new RuntimeException("error");
    }
}