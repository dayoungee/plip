import { ReactComponent as PeopleIcon } from '@/assets/icons/people.svg';
import { ReactComponent as TrashIcon } from '@/assets/icons/trash-can.svg';
import { ReactComponent as StampIcon } from '@/assets/icons/stamp.svg';

import defaultImage from '../../../../public/region/seoul.webp';
import GridItem from './GridItem';
import Button from '@/components/atom/Button';
import Stamp from './Stamp';
import { useState } from 'react';

type Props = {
  isEnd?: boolean;
};

const MyTripCard = ({ isEnd }: Props) => {
  const [endTrip, setEndTrip] = useState(isEnd);

  const onToggleEndTripHandler = () => {
    setEndTrip(!endTrip);
  };

  return (
    <div className="relatvie mt-4 flex h-[220px] w-full gap-16 rounded-lg border p-4 drop-shadow-lg">
      <span className="absolute left-0 top-0 z-30 h-[40px] w-[40px] items-center rounded-[100%] border bg-[#4568DC] text-center text-[8px] leading-[36px]  text-white">
        D-10
      </span>
      <div id="img" className="relative mr-4 shrink-0 ">
        <img
          src={defaultImage}
          alt="region"
          width={280}
          height={180}
          className={endTrip ? 'opacity-50' : ''}
        />
        {endTrip && <Stamp />}
      </div>
      <div id="content" className="flex w-full flex-col">
        {/* 상단 */}
        <div className="flex w-full flex-1 pt-4">
          <div className="grid flex-1 grid-cols-2 items-center gap-2">
            <GridItem title="여행이름" content="즐거운 여행 레츠고!" editable={true} />
            <GridItem title="마지막 수정일" content="2023.07.01" />
            <GridItem title="우리 여행가요!" content="2023.07.12~2023.07.14" />
            <GridItem title="여행장소" content="20" />
            <GridItem title={<PeopleIcon width={16} height={16} />} content="6" />
          </div>

          <div className="flex w-[120px] items-center justify-center gap-4">
            <Button hovercolor={'default'} className="p-0" onClick={onToggleEndTripHandler}>
              <StampIcon fill={endTrip ? '#4568DC' : ''} />
            </Button>
            <Button hovercolor={'default'} className="p-0">
              <TrashIcon />
            </Button>
          </div>
        </div>
        {/* 하단 */}
        <div className="flex h-[80px] w-full items-center gap-16">
          <Button
            variant={'ring'}
            hovercolor={'default'}
            className="h-[50px] w-[120px] text-sm text-zinc-900"
          >
            여행일지
          </Button>
          <Button
            variant={'ring'}
            hovercolor={'default'}
            className="h-[50px] w-[120px] text-sm text-zinc-900"
          >
            일정상세보기
          </Button>
          <Button
            variant={'ring'}
            hovercolor={'default'}
            className="h-[50px] w-[120px] text-sm text-zinc-900"
          >
            일정 수정
          </Button>
          <Button
            variant={'ring'}
            hovercolor={'default'}
            className="h-[50px] w-[120px] text-sm text-zinc-900"
          >
            일정 공유
          </Button>
        </div>
      </div>
    </div>
  );
};

export default MyTripCard;
