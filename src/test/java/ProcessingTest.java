import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ProcessingTest {

    @org.junit.jupiter.api.Test
    void toValidForm1() {

      String result = Processing.toValidForm("REJECTS_CHANGE_OFC_CYCLE");
        Assert.assertEquals(result,"REJECTS_CHANGE_OFC_CYCLE" );
    }

    @org.junit.jupiter.api.Test
    void toValidForm2() {

        String result = Processing.toValidForm(
                "AR_UB_CYCLE_LIST (AR_UB_CYC_LIST)");
        Assert.assertEquals(result,"AR_UB_CYC_LIST" );
    }

    @org.junit.jupiter.api.Test
    void toValidForm3() {

        String result = Processing.toValidForm(
                "CSM SWITCH COMMAND");
        Assert.assertEquals(result,"CSM SWITCH COMMAND" );
    }


    @Test
    void GetValidNameJob() {
        String result = Processing.GetValidNameJob(
                "OP_JOB_MAN ( daemon )");
        Assert.assertEquals(result,"OP_JOB_MAN" );
    }

    @Test
    void GetValidNameJob2() {
        String result = Processing.GetValidNameJob(
                "OP_JOB_MAN");
        Assert.assertEquals(result,"OP_JOB_MAN" );
    }
}