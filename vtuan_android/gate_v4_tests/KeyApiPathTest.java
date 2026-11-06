package com.vtuan.gate;

/** CI-only JVM validation of the independently testable API routing helper. */
public final class KeyApiPathTest {
  private static int assertions=0;

  private static void expect(boolean condition,String note){
    assertions++;
    if(!condition) throw new AssertionError(note);
  }

  public static void main(String[] args){
    expect("/api/activate".equals(KeyApiPath.endpoint("",false)),"Admin activation");
    expect("/api/verify".equals(KeyApiPath.endpoint(null,true)),"Admin verification");
    expect("/api/sv/ctv-01/activate".equals(KeyApiPath.endpoint("ctv-01",false)),"CTV activation");
    expect("/api/sv/ctv-01/verify".equals(KeyApiPath.endpoint(" ctv-01 ",true)),"CTV verification");
    expect(KeyApiPath.isValidServer("a-b"),"Valid 3 char ID");
    expect(KeyApiPath.isValidServer("abcdefghijklmnopqrstuv0123456789"),"Valid 32 char ID");
    for(String invalid: new String[]{"../admin","a/b","CTv","ab","a b","x?key=xx",
                                     "a%2fb", "a".repeat(33)}){
      expect(!KeyApiPath.isValidServer(invalid),"Reject invalid server ID: "+invalid);
      boolean failed=false;
      try {KeyApiPath.endpoint(invalid,false);}
      catch(IllegalArgumentException expected){failed=true;}
      expect(failed,"Invalid ID must never enter URL");
    }
    System.out.println("PASS: "+assertions+" key API routing assertions");
  }
}
