package test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ LineTest.class, GetText.class, InsertText.class, CreateTextLines.class, DeleteText.class, DocumentOffset.class, FindText.class })
public class AllTests {

}
