package com.sinux.mq.client.util;


import java.util.Date;
import java.text.*;
import java.io.*;


/***********************************************************

**********************************************************/


public class CommonFun
{

    public CommonFun()
    {

    }


    /***************************************************************************
     * ����:������������֮������������
     * @param dOld   -������
     * @param dNew   -������
     * @return       ����=���������������������ʱ��
     ****************************************************************************/
    public static long compareDate(Date dOld,Date dNew)  throws Exception
    {
        long lSeconds = -1;

        if (null == dOld || null == dNew)
        {
            throw new Exception("dOld is null or dNew is null");
        }
        try
        {
          
            lSeconds = (dNew.getTime() - dOld.getTime())/1000;
            
            return lSeconds;

        }
        catch (Exception exc)
        {
             throw exc;
        }
    }

    /********************************************************
     * <p>����:��ʽ������</p>
     * <p>����: </p>
     * <p>������:</p>
     * @param
     * @param ����:
     * @param      oDate   -Ҫ��ʽ��������
     * @param      bFlag   -��ʽ������:False=��ʽ����yyyy-mm-dd,True=��ʽ����yyyy-mm-dd hh:mm:ss
     * @param ���:
     * @param      ��    =ʧ��
     * @param      ����  =��ʽ�����ַ���
     *********************************************************/
    public static String dateFormat(Date oDate,boolean bFlag) throws Exception
    {
        String sRet="";
        java.text.SimpleDateFormat formatter;

        if(bFlag == true)
            formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        else
            formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");

        sRet = formatter.format(oDate);

        return sRet;

    }

    /**************************************************************
     * <p>����:��UniCode��ת��ΪAscii��</p>
     * <p>����: </p>
     * <p>������:</p>
     *
     * @param sUniCode -�����UniCode���ַ���
     * @return Ascii���ַ���
     *************************************************************/
    public String Un2Ascii(String sUniCode)
    {
        if(sUniCode == null)
            return "";

        String sRet = null;
        try
        {
            byte[] byteTemp = sUniCode.getBytes();
            sRet = new String(byteTemp,"ISO8859_1");
            return sRet;

        }
        catch(Exception ue)
        {
            System.out.println("Uni2Ascii:" + ue.getMessage());
            return sRet;
        }
    }

    /****************************************************************
     * <p>����:��sAscII��ת��ΪUniCode��
     * <p>����: </p>
     * <p>������:</p>
     * @param sAscII -AscII���ַ���
     * @return UniCode���ַ���
     ***************************************************************/
    public String Ascii2Un(String sAscII)
    {
        if(sAscII==null)return "";
        String sRet = null;
        try
        {
            byte[] byteTemp = sAscII.getBytes("ISO8859_1");
            sRet = new String(byteTemp);
            return sRet;

        }
        catch(Exception ue)
        {
            System.out.println("Ascii2Un:" + ue);
            return sRet;
        }
    }


     /****************************************************************
      * <p>����:���ַ���sourse�е�Oldstr�滻Ϊnewstr
      * <p>����: </p>
      * <p>������:</p>
      * @param sSource -Դ�ַ���
      * @param sOld    -Դ������Ҫ���滻���ַ���
      * @param sNew    -�滻�ɵ����ַ���
      * @return �滻������ַ���
      *****************************************************************/
    public static String replace(String sSource,String sOld,String sNew)
    {
        if(sSource == null || sOld == null|| sNew==null)return "";
        String res = "",tmps=sSource;
        try
        {
            int i;
            while ((i=tmps.indexOf(sOld))!=-1)
            {
                res = res + tmps.substring(0,i)+sNew;
                tmps = tmps.substring(i+sOld.length(),tmps.length());
            }
            res = res + tmps;
            }catch(Exception ue)
            {
                System.out.println("Replace:"+ue);
            }
            return res;
    }
    /**�ļ�׷�Ӻ���
     * @function FileAppend
     * @param sSrcFile
     * @param sDesFile
     * @return  0:�ɹ� -1:ʧ��
     */
    public static int FileAppend(String sSrcFile,String sDesFile)
    {
        File oSrcFile,oDesFile;
        byte[]  copybuf;
        RandomAccessFile oSrcStream = null,oDesStream = null;

        int  NORMALBUFSIZE = 1024*1024;//64K�ֽ�
        int ret = 0;
        oSrcFile = new File(sSrcFile);
       
        oDesFile = new File(sDesFile);
        if(!oSrcFile.exists())
        	return -1;
        if(!oDesFile.exists())
        	return -1;
        long desFileLength = oDesFile.length();
        try
        {
            oSrcStream = new RandomAccessFile(oSrcFile,"r");
            oDesStream = new RandomAccessFile(oDesFile,"rw");
            copybuf = new byte[NORMALBUFSIZE];
            int writeLength = 0;
            for(int iOffset = 0; iOffset < oSrcFile.length();)
            {
            	ByteBuffer.memset(copybuf,(byte)0x00);
                if(oSrcFile.length() - iOffset > NORMALBUFSIZE)
                	writeLength = NORMALBUFSIZE;
                else
                	writeLength = (int)oSrcFile.length() - iOffset;

                oSrcStream.seek((long)(iOffset));
                oSrcStream.readFully(copybuf,0,writeLength);
                oDesStream.seek((long)(desFileLength+iOffset));
                oDesStream.write(copybuf,0,writeLength);
                iOffset += writeLength;
            }

        }
        catch(Exception e)
        {
            System.out.println("Error occur in FileAppend Method,Exception Message:"+e.getMessage());
            ret = -1;
        }
        finally
        {
            try
            {
                if(oSrcStream!=null)
                    oSrcStream.close();
                if(oDesStream!=null)
                    oDesStream.close();
            }
            catch(Exception exc)
            {
                ret = -1;
            }
        }
        return ret;
    }
    /**�ļ����к���
     * @function FileCopy
     * @param sSrcFile
     * @param sDesFile
     * @return  true:�ɹ� false:ʧ��
     */
    public static boolean FileCut(String sSrcFile,String sDesFile)
    {
        File oSrcFile,oDesFile;
        oSrcFile = new File(sSrcFile);
        if(!oSrcFile.exists())
            return false;
        oDesFile = new File(sDesFile);
        if(!oDesFile.getParentFile().exists()){
        	oDesFile.getParentFile().mkdirs();
        }
        return oSrcFile.renameTo(oDesFile);
    }
    
    /****************************************************************
     * <p>����:��double����ֵ�������룬����String��
     * <p>����: </p>
     * <p>������:</p>
     * @param dSource -ԴDouble����ֵ
     * @param iDig    -С������
     * @return
     *****************************************************************/
    public String fmtdouble(double dSource,int iDig)
    {
        NumberFormat tmpformat = NumberFormat.getNumberInstance();
        String resstr="";
        String tmpstr="";
        tmpformat.setMinimumFractionDigits(iDig);
        tmpformat.setMaximumFractionDigits(iDig);
        resstr = tmpformat.format(dSource);
        for(int i=0;i<resstr.length();i++)
        {
            if(resstr.charAt(i)!=',')tmpstr = tmpstr + resstr.charAt(i);
        }
        return tmpstr;
    }


    /******************************************************************
     *��String��Double��ֵ�������룬����String��
     * @param sDouble
     * @param iDig
     * @return
     ******************************************************************/
    public String fmtdouble(String sDouble,int iDig)
    {
        double sourse=0;
        if(sDouble == null)
            sDouble="";
        if(sDouble.equals(""))
            sDouble="0";
        try
        {
            sourse = Double.valueOf(sDouble).doubleValue();
            }catch(NumberFormatException e)
            {
                sourse=0;
            }
            NumberFormat tmpformat = NumberFormat.getNumberInstance();
            String resstr="";
            String tmpstr="";
            tmpformat.setMinimumFractionDigits(iDig);
            tmpformat.setMaximumFractionDigits(iDig);
            resstr = tmpformat.format(sourse);
            for(int i=0;i<resstr.length();i++)
            {
                if(resstr.charAt(i)!=',')tmpstr = tmpstr + resstr.charAt(i);
            }
            return tmpstr;
    }

    /*******************************************************************
     * <p>����:��Double����ֵ�������룬����String�ͣ���ǧ��λ�ָ��2,323,234,343,343.00
     * <p>����: </p>
     * <p>������:</p>
     * @param dSource
     * @param iDig
     * @return
     ******************************************************************/
    public String fmtKiloDouble(double dSource,int iDig)
    {
        NumberFormat tmpformat = NumberFormat.getNumberInstance();
        String resstr="";
        tmpformat.setMinimumFractionDigits(iDig);
        tmpformat.setMaximumFractionDigits(iDig);
        resstr = tmpformat.format(dSource);
        return resstr;
    }


    /*******************************************************************
     * <p>����:��String����ֵ�������룬����String�ͣ���ǧ��λ�ָ��2,323,234,343,343.00
     * <p>����: </p>
     * <p>������:</p>
     * @param sDouble
     * @param iDig
     * @return
     *******************************************************************/
    public String fmtKiloDouble(String sDouble,int iDig)
    {
        double sourse=0;
        if(sDouble==null)sDouble="";
        if(sDouble.equals(""))sDouble="0";
        try
        {
            sourse = Double.valueOf(sDouble).doubleValue();
        }
        catch(NumberFormatException e)
        {
            sourse=0;
        }
        NumberFormat tmpformat = NumberFormat.getNumberInstance();
        String resstr="";
        tmpformat.setMinimumFractionDigits(iDig);
        tmpformat.setMaximumFractionDigits(iDig);
        resstr = tmpformat.format(sourse);
        return resstr;
    }

    public String fmtdouble(double sourse)
    {
        return fmtdouble(sourse,2);
    }


    /*******************************************************************
     * ��double����ֵ��Ϊ�ٷ���
     * @param sDouble
     * @return
     *******************************************************************/
    public String fmtbecomePercent(double sDouble)
    {
        String resstr="";
        try
        {
            NumberFormat tmpformat = NumberFormat.getPercentInstance();
            resstr = tmpformat.format(sDouble);
        }
        catch (Exception e)
        {
            System.out.println("fmtbecamepercent:" + e);
        }
        return resstr;
    }

    /*********************************************************************
   * ɾ��Ŀ¼
   * @param String sPath:Ҫɾ����Ŀ¼��
   * @author ����
   ********************************************************************/
  public static void Deldir(String sPath)
  {
      String sFilepath = "";
      File oFile=null,oTmpFile=null;
      File oFiles[]=null;
      int iCount=0,i=0;
      try
      {
          oFile = new File(sPath);
          oFiles = oFile.listFiles();
          iCount = oFiles.length;
          for(i=0;i<iCount;i++)
          {
              oTmpFile = oFiles[i];
              if(oTmpFile.isFile())
              {
                  try
                  {
                      oTmpFile.delete();
                  }
                  catch(Exception exc)
                  {

                  }
              }
              else
              {
                  if(oTmpFile.isDirectory())
                  {
                      sFilepath = oTmpFile.getAbsolutePath();
                      Deldir(sFilepath);
                  }
                  else
                  {
                      try
                      {
                          oTmpFile.delete();
                      }
                      catch(Exception exc)
                      {

                      }
                  }
              }
          }
          oFile.delete();
      }
      catch(Exception exc)
      {

      }
  }

  /*********************************************************************
   * ���ܣ��ַ�������ת��
   * @param sConvert:Ҫת�����ַ���
   * @param ���أ�����ת������ַ���
   * <p>Date        Author      Changes </p>
   * <p>2003/06/06   ����        Created </p>
   ********************************************************************/
  public static String charsetConvert(String sConvert)
  {
      String bRet ="";
      try
      {
         bRet=new String(sConvert.getBytes("ISO-8859-1"),"gb2312");
         //bRet=sConvert;
      }
      catch(Exception e)
      {
          bRet = sConvert;
      }
      return bRet;
  }

  /*********************************************************************
   * ���ܣ��ַ�������ת��
   * @param sConvert:Ҫת�����ַ�������
   * @param ���أ�����ת������ַ�������
   * <p>Date        Author      Changes </p>
   * <p>2003/06/06   ����        Created </p>
   ********************************************************************/
  public static String[] charsetConvert(String[] sConvert)
  {
      String[] sRet = sConvert;
      try
      {
          for (int i=0;i<sRet.length;i++)
              sRet[i]=new String(sConvert[i].getBytes("ISO-8859-1"),"GB2312");
      }
      catch(Exception e)
      {
          sRet = sConvert;
      }
      return sRet;
  }

  /*********************************************************************
   * ���ܣ��жϴ���������Ƿ�Ϸ�
   * @param sDate:ҪУ�������
   * ���أ��жϴ���������Ƿ�Ϸ�
   *  <p>Date        Author      Changes </p>
   * <p>2003/06/06   ����        Created </p>
   ********************************************************************/
  public static String  ValidateDate(String sDate) throws Exception
  {
      String sRetDate ="";
      Exception e=null;

      try
      {
          sDate = sDate.trim();
          if (sDate.compareTo("") !=0 && sDate.length()!=10 && sDate.length()!=8)
          {
              e = new Exception("����ȷ��������,��2002-01-01����20020101��");
              throw e;
              //return sRetDate;
          }
          sRetDate = sDate;
          if(sDate.length()==8)
          {
              sRetDate = sDate.substring(0,4)+"-"+sDate.substring(4,6)+"-"+sDate.substring(6,8);
          }
          //�ж����ڸ�ʽ�Ƿ�Ϸ�
          java.util.Date TempDate=null;
          try
          {
              TempDate = java.sql.Date.valueOf(sRetDate);
          }
          catch(Exception exc)
          {
              exc = new Exception("����ȷ��������,��2002-01-01����20020101��");
              throw exc;
          }

          java.text.SimpleDateFormat formatter=new java.text.SimpleDateFormat("yyyy-MM-dd");
          sRetDate=formatter.format(TempDate);

          int year,month,day;
          year = Integer.parseInt(sRetDate.substring(0,4));
          month = Integer.parseInt(sRetDate.substring(5,7));
          day = Integer.parseInt(sRetDate.substring(8,10));
          if(month==4||month==6||month==9||month==11)
          {
              if (day > 30)
              {
                  e = new Exception(String.valueOf(year)+"���"+String.valueOf(month)+"�·ݵ����ڲ��ܴ���"+String.valueOf(day));
                  throw e;
              }
          }
          if((year%4==0&&year%100!=0)|| (year%400==0))
          {
              if(month==2)
              {
                  if (day > 29)
                  {
                      e = new Exception(String.valueOf(year)+"���2�·ݵ����ڲ��ܴ���"+String.valueOf(day));
                      throw e;
                  }
              }
          }
          else
          {
              if(month==2)
              {
                  if (day > 28)
                  {
                      e = new Exception(String.valueOf(year)+"���2�·ݵ����ڲ��ܴ���"+String.valueOf(day));
                      throw e;
                  }
              }
          }

      }
      catch(Exception exc)
      {
          throw exc;
      }
      return sRetDate;
  }

  /*********************************************************************
   * ȡ�Ӵ���������
   * @param sourceString
   * @param start
   * @param end
   * @param interval
   * @return
   ********************************************************************/
  public static String getString(String sourceString,int start,int end,String interval)
  {
      //as the return value of Function sGetString()
      String resultString = new String();
      //the real start and end position of the retrived substring in the specified source String
      int startPosition,endPosition;
      //as recurrence variable for searching for the real start position of the retrived substring in the specified source String
      int count;
      //the length of the interval string
      int overlap = interval.length();
      //look up the real start position of the retrived substring in the specified source String
      for(count=0,startPosition=0;count<start;count++)
      {
          startPosition = sourceString.indexOf(interval,startPosition);
          //start position beyond the mark,return empty string
          if(startPosition==-1)
              return resultString;
          else
              startPosition = startPosition+overlap;
      }
      endPosition = sourceString.indexOf(interval,startPosition);
      //retrieve the last substring of the specified source string
      if(endPosition==-1)
          resultString = sourceString.substring(startPosition);
      else
          resultString = sourceString.substring(startPosition,endPosition);
      return resultString;
    }
}