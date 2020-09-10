package gaokao.api;


import gaokao.entity.Aspiration;
import gaokao.entity.Student;
import gaokao.response.Response;
import gaokao.utils.StudentInfo;
import gaokao.wrapper.CollegeFacade;
import gaokao.wrapper.StudentFacade;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserSevice {
    @EJB
    private StudentFacade studentFacade;
    @EJB
    private CollegeFacade collegeFacade;


    @Override
    public Response registerApi(String id,String username, String password, int Score) {
        //前端返回结构体
        List<Object> infoList = new ArrayList<>();
        //获得注册学生信息
        Student student=studentFacade.register(id,password,Score,username);
        boolean flag;
        if(student.getStudentID()==id&&student.getStudentPwd()==password){
            flag=true;
            infoList.add(flag);
        }else {
            flag=false;
            infoList.add(flag);
        }
        Response response = new Response("前端显示消息", infoList, 0);
        return response;
    }

    @Override
    public Response loginApi(String id, String password) {

        List<Object> infoList = new ArrayList<>();

        boolean flag= studentFacade.login(id,password);
        //写入是否成功登录
        StudentInfo studentinfo=new StudentInfo();
        infoList.add(flag);
        if(flag){
            studentinfo=studentFacade.getInfo(id);
        }
        String studentname=studentinfo.getName();
        //写入学生姓名
        infoList.add(studentname);
        //写入学生分数
        infoList.add(studentinfo.getScore());
        Response response = new Response("前端显示消息", infoList, 0);
        return response;
    }

    @Override
    public Response isFilledApi(String id) {
        StudentInfo studentinfo=studentFacade.getInfo(id);
        List<Object> infoList = new ArrayList<>();
        boolean flag=false;
        if (studentinfo.getAspirations()!=null){
            flag=true;
        }
        infoList.add(flag);
        Response response = new Response("前端显示消息", infoList, 0);
        return response;
    }

    @Override
    //8 48
    public Response aspirations(String id, List<String> collegelist, List<String> majorinfolist) {
        List<Object> infoList = new ArrayList<>();
        for(int i=0;i<8;i++){
            String[] majorinfo=new String[6];
            int k=0;
            for(int j=i;j<i+6;j++){
                majorinfo[k]=majorinfolist.get(j);
                k++;
            }
            //写入志愿   学校id  学校名  专业信息  分八次写入
            studentFacade.editAspiration(id,collegelist.get(i),majorinfo);
        }
        StudentInfo studentInfo=  studentFacade.getInfo(id);

        boolean flag=true;
        infoList.add(flag);
        Response response = new Response("前端显示消息", infoList, 0);
        return response;
    }

    @Override
    public void admitAspirations() {
        //获得根据分数排名的student列表

        List<Student> studentList=studentFacade.getStudentRank();

        for(Student student : studentList){
            //得到一名学生报的所有专业
            List<Aspiration> aspirationlist=studentFacade.getFilledAspiration(student.getStudentID());

            for(Aspiration aspiration:aspirationlist){
                String[] majorlsit=aspiration.getMajorList();
                boolean flag=false;

                for(int i=0;i< majorlsit.length;i++){
                    if(collegeFacade.queryMajorCapacity(majorlsit[i])){
                        //如果专业有容量  录取
                        collegeFacade.grantAdmission(student.getStudentID(),aspiration.getCollegeID(),majorlsit[i]);
                        flag=true;
                        break;
                    }
                }
                if(flag){
                    break;
                }
            }
        }
    }

    @Override
    public String[] view(String id) {
        String[] result=studentFacade.getAdmission(id);

        return result;
    }
}
