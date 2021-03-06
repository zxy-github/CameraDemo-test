## 载入所需库
import cv2

def style_transfer(pathIn='',
                   model='',
                   width=500,):
    '''
    pathIn: 原始图片的路径
    pathOut: 风格化图片的保存路径
    model: 预训练模型的路径
    width: 设置风格化图片的宽度，默认为None, 即原始图片尺寸
    jpg_quality: 0-100，设置输出图片的质量，默认80，越大图片质量越好
    '''

    ## 读入原始图片，调整图片至所需尺寸，然后获取图片的宽度和高度
    img = cv2.imread(pathIn)
    (img_h, img_w) = img.shape[:2]
    if width is not None:
        img = cv2.resize(img, (width, (width*img_h//img_w)), interpolation=cv2.INTER_CUBIC)
        (h, w) = img.shape[:2]

    ## 从本地加载预训练模型
    print('加载预训练模型......')
    net = cv2.dnn.readNetFromTorch(model)

    ## 将图片构建成一个blob：设置图片尺寸，将各通道像素值减去平均值（比如ImageNet所有训练样本各通道统计平均值）
    ## 然后执行一次前馈网络计算，并输出计算所需的时间
    blob = cv2.dnn.blobFromImage(img, 1.0, (w, h), (103.939, 116.779, 123.680), swapRB=False, crop=False)
    net.setInput(blob)
    output = net.forward()

    ## reshape输出结果, 将减去的平均值加回来，并交换各颜色通道
    output = output.reshape((3, output.shape[2], output.shape[3]))
    output[0] += 103.939
    output[1] += 116.779
    output[2] += 123.680
    output = output.transpose(1, 2, 0)

    ## 输出风格化后的图片
    output = cv2.resize(output, (img_w, img_h), interpolation=cv2.INTER_CUBIC)

    #返回图片
    return output




