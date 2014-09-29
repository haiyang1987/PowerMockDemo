package cn.monstersay;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DemoSpy.class})
public class DemoSpyTest {
    // �������
    DemoSpy demoSpy;
    // ��������
    private String robin = "Robin Li";
    private String jack = "Jack Ma";
    private String pony = "Pony Ma";

    @Before
    public void setUp() throws Exception {
        /**
         * spy��ʾpartial mock������һ����ʵ�Ķ���Ȼ�������Ҫmock��
         * ��mock��ʱ���㻹�ǿ��Ե���ԭ���ķ�����ȡϣ���Ľ����
         */
        demoSpy = PowerMockito.spy(new DemoSpy("noir.zsk"));
    }

    @After
    public void tearDown() throws Exception {

    }

    // Mock���캯��
    // ͬʱչʾ����ƥ��
    @Test
    public void mockConstructor() {
        DemoSpy demo;
        DemoSpy demoSpyRobin = PowerMockito.spy(new DemoSpy("R"));
        DemoSpy demoSpyJack = PowerMockito.spy(new DemoSpy("J"));
        DemoSpy demoSpyPony = PowerMockito.spy(new DemoSpy("P"));
        /**
         * ʹ��"PowerMockito.doReturn(robin).when(demoSpyBaidu.getName());"��д���ᵼ��PowerMock�׳��쳣��
         * org.mockito.exceptions.misusing.UnfinishedStubbingException:
         * Unfinished stubbing detected here:
         * -> at org.powermock.api.mockito.internal.PowerMockitoCore.doAnswer(PowerMockitoCore.java:36)

         * E.g. thenReturn() may be missing.
         * Examples of correct stubbing:
         * when(mock.isOk()).thenReturn(true);
         * when(mock.isOk()).thenThrow(exception);
         * doThrow(exception).when(mock).someVoidMethod();
         * Hints:
         * 1. missing thenReturn()
         * 2. you are trying to stub a final method, you naughty developer!
         * ����������������볢���޸ĵ���˳��
         * 1. doXXX(...).when(...);
         * 2. doXXX(...).when(instanceName).methodCall(...);
         * 3. when(...).thenXXX(...);
         * �����ϲ������ô���ָ�ʽ��Google��û�кܹٷ���˵�����ҵ�����Ǻͱ��ʽ����˳���йء�
         */
        PowerMockito.doReturn(robin).when(demoSpyRobin).getName();
        PowerMockito.doReturn(jack).when(demoSpyJack).getName();
        PowerMockito.doReturn(pony).when(demoSpyPony).getName();
        try {
            // ֱ��ƥ�����eqƥ��
            PowerMockito.whenNew(DemoSpy.class).withArguments("POny").thenReturn(demoSpyPony);
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.eq("PonY")).thenReturn(demoSpyPony);
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.startsWith("pony")).thenReturn(demoSpyPony);
            demo = new DemoSpy("POny");
            Assert.assertEquals(pony, demo.getName());
            demo = new DemoSpy("PonY");
            Assert.assertEquals(pony, demo.getName());
            demo = new DemoSpy("pony ma");
            Assert.assertEquals(pony, demo.getName());
            demo = new DemoSpy("Pony Ma");
            Assert.assertEquals(null, demo);
            // �Զ���ƥ����
            class IsJackStringMatcher extends ArgumentMatcher<String> {
                public boolean matches(Object string) {
                    return ((String)string).equalsIgnoreCase(jack);
                }
            }
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.argThat(new IsJackStringMatcher())).thenReturn(demoSpyJack);
            demo = new DemoSpy("JaCK ma");
            Assert.assertEquals(jack, demo.getName());
            demo = new DemoSpy("jAcK MA");
            Assert.assertEquals(jack, demo.getName());
            // ƥ��String���͵�����ֵ
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.anyString()).thenReturn(demoSpyRobin);
            demo = new DemoSpy("random string");
            Assert.assertEquals(robin, demo.getName());
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    // Mock����static����
    @Test
    public void mockPublicStaticMethod() {
        DemoSpy.setBossName(pony);
        Assert.assertEquals(pony, DemoSpy.getBossName());
        PowerMockito.mockStatic(DemoSpy.class);
        PowerMockito.when(DemoSpy.getBossName()).thenReturn(jack);
        Assert.assertEquals(jack, DemoSpy.getBossName());
    }

    // Mock˽��static����
    @Test
    public void mockPrivateStaticMethod() {
        DemoSpy.setBossName(pony);
        Assert.assertEquals(pony, DemoSpy.getBossName());
        // ������spy���Ա�֤getBossName�����getBossNameInternal��
        PowerMockito.spy(DemoSpy.class);
        try {
            PowerMockito.doReturn(jack).when(DemoSpy.class, "getBossNameInternal");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        Assert.assertEquals(jack, DemoSpy.getBossName());
    }

    // Mock���з���
    @Test
    public void mockPublicMethod() {
        try {
            PowerMockito.doReturn("mocked feedback").when(demoSpy).doFeedback(Mockito.anyBoolean());
            Assert.assertEquals("mocked feedback", demoSpy.doFeedback(false));
        } catch(Exception e) {
            Assert.assertEquals(e.getMessage(), false);
        }
    }

    // Mock���з����׳��쳣
    @Test
    public void mockPublicMethodThrowException() {
        try {
            PowerMockito.doThrow(new IOException("...")).when(demoSpy).doFeedback(Mockito.anyBoolean());
            try {
                demoSpy.doFeedback(true);
                Assert.assertEquals("IOException is expected.", false);
            } catch (IOException e) {
                Assert.assertTrue(true);
            }
        } catch(Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    // UDT���ʹ���
    @Test
    public void mockProcessUDTType() {
        UDT udt = new UDT();
        udt.setName(robin);
        demoSpy.setName(pony);
        demoSpy.processUDTType(udt);
        Assert.assertEquals(pony, udt.getName());
        // ArgumentCaptor
        ArgumentCaptor<UDT> argUDT = ArgumentCaptor.forClass(UDT.class);
        PowerMockito.doNothing().when(demoSpy).processUDTType(argUDT.capture());
        udt.setName(jack);
        demoSpy.processUDTType(udt);
        // ��ΪprocessUDTType���޷���ֵ������ͨ��mockֱ�����ء�
        Assert.assertEquals(jack, udt.getName());
    }

    // Mock������
    @Test
    public void mockMethodBody() {
        UDT udt = new UDT();
        udt.setName(pony);
        ArgumentCaptor<UDT> argUDT = ArgumentCaptor.forClass(UDT.class);
        PowerMockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                UDT udt = (UDT)invocation.getArguments()[0];
                udt.setName(robin);
                // ��Ϊ��void���������Է���null�Ϳ����ˡ�
                return null;
            }
        }).when(demoSpy).processUDTType(argUDT.capture());
        demoSpy.processUDTType(udt);
        Assert.assertEquals(robin, udt.getName());
    }

    // Mock˽�з���
    @Test
    public void mockPrivateMethod() {
        try {
            demoSpy.processInput(pony);
            Assert.assertEquals(pony.toUpperCase(), demoSpy.doFeedback(true));
            PowerMockito.doReturn(jack).when(demoSpy, "doFeedbackInternal", true);
            Assert.assertEquals(jack, demoSpy.doFeedback(true));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    // Mock˽�з����׳��쳣
    @Test
    public void mockPrivateMethodThrowException() {
        try {
            PowerMockito.doThrow(new IOException("...")).when(demoSpy, "doFeedbackInternal", true);
            demoSpy.processInput(pony);
            Assert.assertEquals(pony, demoSpy.doFeedback(false));
            try {
                demoSpy.doFeedback(true);
                Assert.assertTrue("IOException is expected.", false);
            } catch (IOException e) {
                Assert.assertTrue(true);
            }
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    /**
     * ��⺯������˳��ֻ�Թ��з�����Ч���ʹ���
     */
    @Test
    public void checkCallSequenceAndTimes() {
        demoSpy.mixedOperations();
        InOrder inOrder = Mockito.inOrder(demoSpy);
        inOrder.verify(demoSpy, Mockito.times(2)).step001();
        inOrder.verify(demoSpy, Mockito.times(1)).step002();
        inOrder.verify(demoSpy, Mockito.times(2)).step003();
        inOrder.verify(demoSpy, Mockito.times(1)).step002();
        inOrder.verify(demoSpy, Mockito.times(1)).step003();
        try {
            PowerMockito.verifyPrivate(demoSpy, Mockito.times(1)).invoke("step004Internal");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }
}