import de.militaermiltz.tdv.commands.CommandPrependCommand;
import de.militaermiltz.tdv.commands.CommandUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TestUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class CommandPrependCommandTest {

    CommandPrependCommand cmdPre;
    HashMap<String, Method> methods;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        cmdPre = new CommandPrependCommand();
        methods = TestUtil.getMethods(CommandPrependCommand.class);

        TestUtil.setFinalStaticField(CommandUtil.class, "COMMANDS", Arrays.asList("/playsound", "/enchant", "/xp"));
    }

    @Test
    void isCompleteTest() throws InvocationTargetException, IllegalAccessException {
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/hallo"));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "bFSDH"));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend"));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend "));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend 10"));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10"));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10"));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10"));
        assertTrue((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10 \"\""));
        assertTrue((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10 \"Haus\""));
        assertTrue((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10 \"Ha\"us\""));
        assertTrue((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10 \"Ha\"us\""));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10 \"Haus\" "));
        assertTrue((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10 \"Haus\" /playsound"));
        assertFalse((Boolean) methods.get("isComplete").invoke(cmdPre, "/commandprepend -10 10 10 10 -19 10 \"Haus\" /garNix"));
    }
}