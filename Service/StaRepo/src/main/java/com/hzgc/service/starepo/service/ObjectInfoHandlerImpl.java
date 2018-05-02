package com.hzgc.service.starepo.service;

import com.hzgc.common.service.table.column.ObjectInfoTable;
import com.hzgc.common.service.table.column.SearchRecordTable;
import com.hzgc.common.util.object.ObjectUtil;
import com.hzgc.jni.FaceAttribute;
import com.hzgc.service.starepo.object.*;
import com.hzgc.service.starepo.util.PhoenixJDBCHelper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ObjectInfoHandlerImpl {

    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);

    public ObjectInfoHandlerImpl() {
    }

    /**
     * 针对单个对象信息的添加处理  （外）（李第亮）
     * @param  platformId 表示的是平台的ID， 平台的唯一标识。
     * @param personObject K-V 对，里面存放的是字段和值之间的一一对应关系,
     *               例如：传入一个Map 里面的值如下map.put("idcard", "450722199502196939")
     *               表示的是身份证号（idcard）是450722199502196939，
     *               其中的K 的具体，请参考给出的数据库字段设计
     * @return 返回值为0，表示插入成功，返回值为1，表示插入失败
     */
    public byte addObjectInfo(String platformId, Map<String, Object> personObject) {
        LOG.info("personObject: " + personObject.entrySet().toString());
        java.sql.Connection conn;
        long start = System.currentTimeMillis();
        PersonObject person = PersonObject.mapToPersonObject(personObject);
        person.setPlatformid(platformId);
        LOG.info("the rowkey off this add person is: " + person.getId());

        String sql = "upsert into objectinfo(" + ObjectInfoTable.ROWKEY + ", " + ObjectInfoTable.NAME + ", "
                + ObjectInfoTable.PLATFORMID + ", " + ObjectInfoTable.TAG + ", " + ObjectInfoTable.PKEY + ", "
                + ObjectInfoTable.IDCARD + ", " + ObjectInfoTable.SEX + ", " + ObjectInfoTable.PHOTO + ", "
                + ObjectInfoTable.FEATURE + ", " + ObjectInfoTable.REASON + ", " + ObjectInfoTable.CREATOR + ", "
                + ObjectInfoTable.CPHONE + ", " + ObjectInfoTable.CREATETIME + ", " + ObjectInfoTable.UPDATETIME + ", "
                + ObjectInfoTable.IMPORTANT + ", " + ObjectInfoTable.STATUS
                + ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = null;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = new ObjectInfoHandlerTool().getStaticPrepareStatementV1(conn, person, sql);
            pstm.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, null);
        }
        LOG.info("添加一条数据到静态库花费时间： " + (System.currentTimeMillis() - start));
        //数据变动，更新objectinfo table 中的一条数据,表示静态库中的数据有变动
        new ObjectInfoHandlerTool().updateTotalNumOfHbase();
        return 0;
    }

    /**
     * 删除对象的信息  （外）（李第亮）
     * @param rowkeys 具体的一个人员信息的ID，值唯一
     * @return 返回值为0，表示删除成功，返回值为1，表示删除失败
     */
    public int deleteObjectInfo(List<String> rowkeys) {
        LOG.info("rowKeys: " + rowkeys);
        // 获取table 对象，通过封装HBaseHelper 来获取
        long start = System.currentTimeMillis();
        java.sql.Connection conn = null;
        String sql = "delete from objectinfo where " + ObjectInfoTable.ROWKEY + " = ?";
        PreparedStatement pstm = null;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i < rowkeys.size(); i++) {
                pstm.setString(1, rowkeys.get(i));
                pstm.executeUpdate();
                if (i % 10 == 0) {
                    conn.commit();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, null);
        }
        LOG.info("删除静态信息库的" + rowkeys.size() + "条数据花费时间： " + (System.currentTimeMillis() - start));
        //数据变动，更新objectinfo table 中的一条数据,表示静态库中的数据有变动
        new ObjectInfoHandlerTool().updateTotalNumOfHbase();
        return 0;
    }

    /**
     * 修改对象的信息   （外）（李第亮）
     * @param personObject K-V 对，里面存放的是字段和值之间的一一对应关系，参考添加里的描述
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    public int updateObjectInfo(Map<String, Object> personObject) {
        LOG.info("personObject: " + personObject.entrySet().toString());
        long start = System.currentTimeMillis();
        String thePassId = (String) personObject.get(ObjectInfoTable.ROWKEY);
        java.sql.Connection conn = null;
        if (thePassId == null) {
            LOG.info("the pass Id can not be null....");
            return 1;
        }
        PreparedStatement pstm = null;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            ConcurrentHashMap<String, CopyOnWriteArrayList<Object>> sqlAndSetValues = ParseByOption.getUpdateSqlFromPersonMap(personObject);
            String sql = null;
            CopyOnWriteArrayList<Object> setValues = new CopyOnWriteArrayList<>();
            for (Map.Entry<String, CopyOnWriteArrayList<Object>> entry : sqlAndSetValues.entrySet()) {
                sql = entry.getKey();
                setValues = entry.getValue();
            }
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i < setValues.size(); i++) {
                pstm.setObject(i + 1, setValues.get(i));
            }
            pstm.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, null);
        }
        LOG.info("更新rowkey为: " + thePassId + "数据花费的时间是: " + (System.currentTimeMillis() - start));
        //数据变动，更新objectinfo table 中的一条数据,表示静态库中的数据有变动
        new ObjectInfoHandlerTool().updateTotalNumOfHbase();
        return 0;
    }

    /**
     * 根据rowkey 进行查询 （外）
     * @param id  标记一条对象信息的唯一标志。
     * @return  返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    public ObjectSearchResult searchByRowkey(String id) {
        long start = System.currentTimeMillis();
        java.sql.Connection conn = null;
        PreparedStatement pstm = null;
        ObjectSearchResult result = new ObjectSearchResult();
        PersonObject person;
        ResultSet resultSet = null;
        try {
            String sql = "select * from " + ObjectInfoTable.TABLE_NAME + " where id = ?";
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, id);
            resultSet = pstm.executeQuery();
            person = new ObjectInfoHandlerTool().getPersonObjectFromResultSet(resultSet);
            result.setSearchStatus(0);
            List<PersonSingleResult> personSingleResults = new ArrayList<>();
            PersonSingleResult personSingleResult = new PersonSingleResult();
            personSingleResult.setSearchNums(1);
            List<PersonObject> persons = new ArrayList<>();
            persons.add(person);
            personSingleResult.setPersons(persons);
            personSingleResults.add(personSingleResult);
            result.setFinalResults(personSingleResults);
        } catch (SQLException e) {
            result.setSearchStatus(1);
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        }
        LOG.info("获取一条数据的时间是：" + (System.currentTimeMillis() - start));
        return result;
    }

    /**
     * 可以匹配精确查找，以图搜索人员信息，模糊查找   （外）（李第亮）
     * @param pSearchArgsModel 搜索参数的封装
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    public ObjectSearchResult getObjectInfo(PSearchArgsModel pSearchArgsModel) {
        LOG.info("pSearchArgsModel: " + pSearchArgsModel);
        long start = System.currentTimeMillis();
        java.sql.Connection conn = null;
        // 总的结果
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        String searchTotalId = UUID.randomUUID().toString().replace("-", "");
        objectSearchResult.setSearchTotalId(searchTotalId);
        List<PersonSingleResult> finalResults = new ArrayList<>();
        conn = PhoenixJDBCHelper.getInstance().getConnection();

        if (conn == null) {
            ObjectSearchResult objectSearchResultError = new ObjectSearchResult();
            objectSearchResult.setSearchStatus(1);
            objectSearchResult.setSearchTotalId(UUID.randomUUID().toString().replace("-", ""));
            objectSearchResult.setFinalResults(finalResults);
            return objectSearchResultError;
        }

        //封装的sql 以及需要设置的值
        Map<String, List<Object>> finalSqlAndValues = ParseByOption.getSqlFromPSearchArgsModel(conn, pSearchArgsModel);

        PreparedStatement pstm = null;
        ResultSet resultSet = null;

        // 取出封装的sql 以及需要设置的值，进行sql 查询
        if (finalSqlAndValues == null) {
            objectSearchResult.setSearchStatus(1);
            LOG.info("创建sql 失败，请检查代码");
            return objectSearchResult;
        }
        for (Map.Entry<String, List<Object>> entry : finalSqlAndValues.entrySet()) {
            String sql = entry.getKey();
            List<Object> setValues = entry.getValue();
            try {
                // 实例化pstm 对象，并且设置值，
                pstm = conn.prepareStatement(sql);
                for (int i = 0; i < setValues.size(); i++) {
                    pstm.setObject(i + 1, setValues.get(i));
                }
                resultSet = pstm.executeQuery();
                Map<String, byte[]> photos = pSearchArgsModel.getImages();
                Map<String, FaceAttribute> faceAttributeMap = pSearchArgsModel.getFaceAttributeMap();
                // 有图片的情况下
                if (photos != null && photos.size() != 0
                        && faceAttributeMap != null && faceAttributeMap.size() != 0
                        && faceAttributeMap.size() == photos.size()) {
                    if (!pSearchArgsModel.isTheSameMan()) {  // 不是同一个人
                        // 分类的人
                        Map<String, List<PersonObject>> personObjectsMap = new HashMap<>();

                        List<PersonObject> personObjects = new ArrayList<>();
                        PersonObject personObject;
                        List<String> types = new ArrayList<>();
                        int lable = 0;
                        while (resultSet.next()) {
                            String type = resultSet.getString("type");
                            if (!types.contains(type)) {
                                types.add(type);
                                if (types.size() > 1) {
                                    personObjectsMap.put(types.get(lable), personObjects);
                                    personObjects = new ArrayList<>();
                                    lable++;
                                }
                            }
                            personObject = new PersonObject();
                            personObject.setId(resultSet.getString(ObjectInfoTable.ROWKEY));
                            personObject.setPkey(resultSet.getString(ObjectInfoTable.PKEY));
                            personObject.setPlatformid(resultSet.getString(ObjectInfoTable.PLATFORMID));
                            personObject.setName(resultSet.getString(ObjectInfoTable.NAME));
                            personObject.setSex(resultSet.getInt(ObjectInfoTable.SEX));
                            personObject.setIdcard(resultSet.getString(ObjectInfoTable.IDCARD));
                            personObject.setCreator(resultSet.getString(ObjectInfoTable.CREATOR));
                            personObject.setCphone(resultSet.getString(ObjectInfoTable.CPHONE));
                            personObject.setUpdatetime(resultSet.getTimestamp(ObjectInfoTable.UPDATETIME));
                            personObject.setCreatetime(resultSet.getTimestamp(ObjectInfoTable.CREATETIME));
                            personObject.setReason(resultSet.getString(ObjectInfoTable.REASON));
                            personObject.setTag(resultSet.getString(ObjectInfoTable.TAG));
                            personObject.setImportant(resultSet.getInt(ObjectInfoTable.IMPORTANT));
                            personObject.setStatus(resultSet.getInt(ObjectInfoTable.STATUS));
                            personObject.setSim(resultSet.getFloat(ObjectInfoTable.RELATED));
                            personObject.setLocation(resultSet.getString(ObjectInfoTable.LOCATION));
                            personObjects.add(personObject);
                        }
                        // 封装最后需要返回的结果
                        for (Map.Entry<String, List<PersonObject>> entryVV : personObjectsMap.entrySet()) {
                            String key = entryVV.getKey();
                            List<PersonObject> persons = entryVV.getValue();
                            PersonSingleResult personSingleResult = new PersonSingleResult();
                            personSingleResult.setSearchRowkey(searchTotalId + key);
                            personSingleResult.setSearchNums(persons.size());
                            personSingleResult.setPersons(persons);
                            List<byte[]> photosTmp = new ArrayList<>();
                            photosTmp.add(photos.get(key));
                            personSingleResult.setSearchPhotos(photosTmp);
                            if (personSingleResult.getPersons() != null || personSingleResult.getPersons().size() != 0) {
                                finalResults.add(personSingleResult);
                            }
                        }
                    } else {  // 是同一个人
                        PersonSingleResult personSingleResult = new PersonSingleResult();
                        personSingleResult.setSearchRowkey(searchTotalId);

                        List<byte[]> searchPhotos = new ArrayList<>();
                        for (Map.Entry<String, byte[]> entryV1 : photos.entrySet()) {
                            searchPhotos.add(entryV1.getValue());
                        }
                        personSingleResult.setSearchPhotos(searchPhotos);
                        // 封装personSingleResult
                        new ObjectInfoHandlerTool().getPersonSingleResult(personSingleResult, resultSet, true);
                        if (personSingleResult.getPersons() != null || personSingleResult.getPersons().size() != 0) {
                            finalResults.add(personSingleResult);
                        }
                    }
                } else { // 没有图片的情况下
                    PersonSingleResult personSingleResult = new PersonSingleResult();   // 需要进行修改
                    personSingleResult.setSearchRowkey(searchTotalId);
                    //封装personSingleResult
                    new ObjectInfoHandlerTool().getPersonSingleResult(personSingleResult, resultSet, false);
                    if (personSingleResult.getPersons() != null || personSingleResult.getPersons().size() != 0) {
                        finalResults.add(personSingleResult);
                    }
                }
            } catch (SQLException e) {
                objectSearchResult.setSearchStatus(1);
                e.printStackTrace();
                return objectSearchResult;
            }
        }

        objectSearchResult.setFinalResults(finalResults);
        objectSearchResult.setSearchStatus(0);

        LOG.info("总的搜索时间是: " + (System.currentTimeMillis() - start));
        new ObjectInfoHandlerTool().saveSearchRecord(conn, objectSearchResult);
        Integer pageSize = pSearchArgsModel.getPageSize();
        Integer startCount = pSearchArgsModel.getStart();
        if (startCount != null && pageSize != null) {
            new ObjectInfoHandlerTool().formatTheObjectSearchResult(objectSearchResult, startCount, pageSize);
        }
        PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        LOG.info("***********************");
        LOG.info(objectSearchResult);
        LOG.info("***********************");
        return objectSearchResult;
    }


    /**
     * 根据rowkey 返回人员的照片
     * @param rowkey 人员在对象信息库中的唯一标志。
     * @return 图片的byte[] 数组
     */
    public byte[] getPhotoByKey(String rowkey) {
        String sql = "select photo from " + ObjectInfoTable.TABLE_NAME + " where id = ?";
        java.sql.Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        byte[] photo;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, rowkey);
            resultSet = pstm.executeQuery();
            resultSet.next();
            photo = resultSet.getBytes(ObjectInfoTable.PHOTO);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        }
        return photo;
    }

    private static ObjectSearchResult getObjectSearchResultError(String errorMsg) {
        ObjectSearchResult objectSearchResultError = new ObjectSearchResult();
        objectSearchResultError.setSearchStatus(1);
        LOG.info(errorMsg);
        return objectSearchResultError;
    }

    /**
     * 根据传过来的搜索rowkey 返回搜索记录 （外） （李第亮）
     * @param  searchRecordOpts 历史查询参数
     * @return  返回一个ObjectSearchResult 对象，
     * @author 李第亮
     * 里面包含了本次查询ID，查询成功标识，
     * 查询照片ID（无照片，此参数为空），结果数，人员信息列表
     */
    public ObjectSearchResult getRocordOfObjectInfo(SearchRecordOpts searchRecordOpts) {
        LOG.info("searchRecordOpts: " + searchRecordOpts);
        // 传过来的参数中为空，或者子查询为空，或者子查询大小为0，都返回查询错误。
        if (searchRecordOpts == null) {
            return getObjectSearchResultError("SearchRecordOpts 为空，请确认参数是否正确.");
        }
        // 总的searchId
        List<SubQueryOpts> subQueryOptsList = searchRecordOpts.getSubQueryOptsList();
        if (subQueryOptsList == null || subQueryOptsList.size() == 0) {
            return getObjectSearchResultError("子查询列表为空，请确认参数是否正确.");
        }

        SubQueryOpts subQueryOpts = subQueryOptsList.get(0);
        if (subQueryOpts == null) {
            return getObjectSearchResultError("子查询对象SubQueryOpts 对象为空，请确认参数是否正确.");
        }

        // 子查询Id
        String subQueryId = subQueryOpts.getQueryId();
        if (subQueryId == null) {
            LOG.info("子查询Id 为空");
            return getObjectSearchResultError("子查询Id 为空，请确认参数是否正确.");
        }

        // 需要分组的pkeys
        List<String> pkeys = subQueryOptsList.get(0).getPkeys();
        // 排序参数
        List<StaticSortParam> staticSortParams = searchRecordOpts.getStaticSortParams();

        // sql 查询语句
        String sql = "select " + SearchRecordTable.ID + ", " + SearchRecordTable.RESULT + " from "
                + SearchRecordTable.TABLE_NAME + " where " + SearchRecordTable.ID + " = ?";

        ObjectSearchResult finnalObjectSearchResult = new ObjectSearchResult();
        List<PersonSingleResult> personSingleResults = new ArrayList<>();

        java.sql.Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, subQueryId);
            resultSet = pstm.executeQuery();
            resultSet.next();
            PersonSingleResult personSingleResult = (PersonSingleResult) ObjectUtil
                    .byteToObject(resultSet.getBytes(SearchRecordTable.RESULT));
            if (personSingleResult != null) {
                List<PersonObject> personObjects = personSingleResult.getPersons();
                List<GroupByPkey> groupByPkeys = new ArrayList<>();
                GroupByPkey groupByPkey;
                if (personObjects != null && staticSortParams != null && staticSortParams.contains(StaticSortParam.PEKEY)) {
                    Map<String, List<PersonObject>> groupingByPkeys = personObjects.stream()
                            .collect(Collectors.groupingBy(PersonObject::getPkey));
                    for (Map.Entry<String, List<PersonObject>> entry : groupingByPkeys.entrySet()) {
                        groupByPkey = new GroupByPkey();
                        String pkey = entry.getKey();
                        groupByPkey.setPkey(pkey);
                        List<PersonObject> personObjectList = entry.getValue();
                        // 对结果进行排序
                        new ObjectInfoHandlerTool().sortPersonObject(personObjectList, staticSortParams);

                        // 如果指定了需要返回的Pkey
                        if (pkeys != null && pkeys.size() > 0 && pkeys.contains(pkey)) {
                            groupByPkey.setPersons(personObjectList);
                            groupByPkeys.add(groupByPkey);
                            continue;
                        }
                        if (pkeys == null || pkeys.size() == 0) {
                            groupByPkey.setPersons(personObjectList);
                            groupByPkeys.add(groupByPkey);
                        }
                    }
                    personSingleResult.setGroupByPkeys(groupByPkeys);
                    personSingleResult.setPersons(null);
                } else if (personObjects != null && staticSortParams != null && !staticSortParams.contains(StaticSortParam.PEKEY)) {
                    personSingleResult.setGroupByPkeys(null);
                    new ObjectInfoHandlerTool().sortPersonObject(personObjects, staticSortParams);
                    personSingleResult.setPersons(personObjects);
                }
            }
            personSingleResults.add(personSingleResult);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        }
        finnalObjectSearchResult.setSearchStatus(0);
        finnalObjectSearchResult.setFinalResults(personSingleResults);
        int pageSize = searchRecordOpts.getSize();
        int start = searchRecordOpts.getStart();
        new ObjectInfoHandlerTool().formatTheObjectSearchResult(finnalObjectSearchResult, start, pageSize);
        LOG.info("***********************");
        LOG.info(finnalObjectSearchResult);
        LOG.info("***********************");
        return finnalObjectSearchResult;
    }

    /**
     * 根据穿过来的rowkey 返回照片 （外） （李第亮）
     * @param rowkey 即Hbase 数据库中的rowkey，查询记录唯一标志
     * @return 返回查询的照片
     */
    public byte[] getSearchPhoto(String rowkey) {
        return null;
    }
}