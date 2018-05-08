package ut.com.softwaredevtools.standbot;

import org.junit.Test;
import com.softwaredevtools.standbot.api.MyPluginComponent;
import com.softwaredevtools.standbot.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}