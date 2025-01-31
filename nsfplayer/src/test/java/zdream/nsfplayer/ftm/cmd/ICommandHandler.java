package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.FtmPlayerConsole;


public interface ICommandHandler {

    /**
     * Indicates which operations can be handled by this console command-like handler.
     *
     * @return Please note that if the string is in English it must be lowercase.
     */
    String[] canHandle();

    /**
     * processing operation
     *
     * @param args A parameter, entered by the user or other inputs, that explains the purpose of the operation.<br>
     *             Similar to the input parameters of the <code>main()</code> method.<br>
     *             <code>args[0]</code> must exist.
     *             It must be equivalent to an element of the array returned by the <code>canHandle()</code> method,
     *             and must be all lowercase.
     * @param env  environment
     */
    void handle(String[] args, FtmPlayerConsole env);
}
